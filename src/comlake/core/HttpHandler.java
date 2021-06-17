/*
 * HTTP request handler
 * Copyright (C) 2021  Nguyễn Gia Phong
 *
 * This file is part of comlake.core.
 *
 * comlake.core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation.
 *
 * comlake.core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with comlake.core.  If not, see <https://www.gnu.org/licenses/>.
 */

package comlake.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import com.google.gson.Gson;

import comlake.core.Configuration;
import comlake.core.Ingestor;
import comlake.core.db.Database;
import comlake.core.db.PostgreSQL;
import comlake.core.fs.FileSystem;
import comlake.core.fs.InterPlanetaryFileSystem;

public class HttpHandler {
    static final Gson gson = new Gson();
    static final IFn require = Clojure.var("clojure.core", "require");

    private IFn parseAst;
    private FileSystem fs;
    private Database db;
    private Ingestor ingestor;

    public HttpHandler(Configuration cfg) {
        require.invoke(Clojure.read("comlake.core.db.qast"));
        parseAst = Clojure.var("comlake.core.db.qast", "json-to-psql");

        // TODO: stop hard-coding these
        fs = new InterPlanetaryFileSystem(cfg.ipfsMultiAddr);
        db = new PostgreSQL(cfg.psqlUrl, cfg.psqlUser, cfg.psqlPasswd);
        ingestor = new Ingestor(fs, db);
    }

    /** Construct a Ring response. **/
    static Map respond(int status, Map headers, Object body) {
        return Map.of(
            Clojure.read(":status"), status,
            Clojure.read(":headers"), headers,
            Clojure.read(":body"), body);
    }

    /** Construct headers with only Content-Type. **/
    static Map contentType(String type) {
        return Map.of("Content-Type", type);
    }

    /** Wrap given error string in a Ring JSON response. **/
    public static Map error(Object err, int status) {
        var body = gson.toJson(Map.of("error", err));
        return respond(status, contentType("application/json"), body);
    }

    /**
     * Wrap given error string in a Ring JSON response,
     * defaulting status to 400 Bad Request.
    **/
    static Map error(Object err) {
        if (err == null)
            return error("internal server error", 500);
        return error(err, 400);
    }

    /** Create and return an empty directory. **/
    public Map mkdir() {
        var cid = fs.mkdir();
        db.insertDirectory(cid); // TODO: check status
        var json = gson.toJson(Map.of("cid", cid));
        return respond(200, contentType("application/json"), json);
    }

    /** Write file to underlying file system. **/
    public Map save(Map<String, String> headers, InputStream body) {
        // TODO: handle size mismatch
        var cid = fs.add(body);
        if (cid == null)
            return error("empty data");

        var type = headers.get("content-type"); // TODO: check null
        db.insertFile(cid, type); // TODO: check status

        var json = gson.toJson(Map.of("cid", cid));
        return respond(200, contentType("application/json"), json);
    }

    /** Copy file inside a directory. **/
    public Map cp(InputStream body) {
        var reader = new InputStreamReader(body);
        var args = (Map<String, String>) gson.fromJson(reader, Map.class);
        var cid = fs.cp(args.get("src"), args.get("dest"), args.get("path"));
        if (cid == null)
            return error("dest is not a directory");

        var json = gson.toJson(Map.of("cid", cid));
        return respond(200, contentType("application/json"), json);
    }

    /** Ingest data from the given request and return appropriate response. **/
    public Map add(InputStream body) {
        var reader = new InputStreamReader(body);
        var dataset = (Map<String, Object>) gson.fromJson(reader, Map.class);
        var missing = Arrays.asList("file", "description", "source", "topics")
            .stream().filter(field -> dataset.get(field) == null)
            .collect(Collectors.toList());
        if (!missing.isEmpty())
            return error(Map.of("missing-metadata", missing));

        var id = db.insertDataset(dataset);
        if (id == null)
            return error(null);

        var json = gson.toJson(Map.of("id", id));
        return respond(200, contentType("application/json"), json);
    }

    /** Return query result as a http response. **/
    public Map find(InputStream ast) {
        var predicate = (String) parseAst.invoke(new InputStreamReader(ast));
        if (predicate == null)
            return error("malformed query");

        var result = db.search(predicate);
        if (result == null)
            return error("failed query");

        var body = gson.toJson(result);
        return respond(200, contentType("application/json"), body);
    }

    /** List content of a file system directory. **/
    public Map ls(String cid) {
        var result = fs.ls(cid);
        if (result == null)
            return error("not a directory");

        var body = gson.toJson(result);
        return respond(200, contentType("application/json"), body);
    }

    /**
     * Forward content from underlying distributed filesystem
     * as a Ring response.
    **/
    public Map get(String cid) {
        var body = fs.fetch(cid);
        if (body == null)
            return error("content not found", 404);
        return respond(200, contentType("application/octet-stream"), body);
    }
}
