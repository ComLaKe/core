/*
 * HTTP request handler
 * Copyright (C) 2021  Nguyễn Gia Phong
 *
 * This file is part of comlake-core.
 *
 * comlake-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation.
 *
 * comlake-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with comlake-core.  If not, see <https://www.gnu.org/licenses/>.
 */

package comlake_core;

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

import comlake_core.Database;
import comlake_core.FileSystem;
import comlake_core.Ingestor;
import comlake_core.InterPlanetaryFileSystem;
import comlake_core.PostgreSQL;

public class HttpHandler {
    static final String[] requiredFields = {"length", "type",
                                            "name", "source", "topics"};
    static final Gson gson = new Gson();
    static final IFn require = Clojure.var("clojure.core", "require");

    private IFn parseAst;
    private FileSystem fs;
    private Database db;
    private Ingestor ingestor;

    public HttpHandler() {
        require.invoke(Clojure.read("comlake-core.qast"));
        parseAst = Clojure.var("comlake-core.qast", "json-to-psql");

        // TODO: stop hard-coding these
        fs = new InterPlanetaryFileSystem("/ip4/127.0.0.1/tcp/5001");
        db = new PostgreSQL("jdbc:postgresql:comlake", "postgres", "postgres");
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

    /** Ingest data from the given request and return appropriate response. **/
    public Map add(Map<String, String> headers, InputStream body) {
        var outcome = ingestor.add(headers, body);
        if (!outcome.ok)
            return error(outcome.error);

        var json = gson.toJson(Map.of("cid", outcome.result));
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
