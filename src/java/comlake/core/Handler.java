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

package comlake.core;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import com.google.gson.Gson;

public class Handler {
    static final String[] requiredFields = {"length", "type",
                                            "name", "source", "topics"};
    static final Gson gson = new Gson();

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
        return error(err, 400);
    }

    /** Ingest data from the given request and return appropriate response. **/
    public static Map add(Map<String, String> headers,
                          ByteArrayInputStream body) {
        var metadata = new HashMap<String, Object>();
        for (var header : headers.entrySet()) {
            var key = header.getKey();
            switch (key) {
            case "content-length":
                metadata.put("length", Integer.parseInt(header.getValue()));
                break;
            case "content-type":
                metadata.put("type", header.getValue());
                break;
            case "x-comlake-length":
            case "x-comlake-type":
                break;  // disarm footgun
            case "x-comlake-topics":
                var topics = header.getValue().split("\\s*,\\s*");
                metadata.put("topics", Arrays.asList(topics));
                break;
            default:
                if (key.startsWith("x-comlake-"))
                    metadata.put(key.substring(10), header.getValue());
            }
        }

        var missing = Arrays.stream(requiredFields)
            .filter(field -> metadata.get(field) == null)
            .collect(Collectors.toList());
        if (!missing.isEmpty())
            return error(Map.of("missing-metadata", missing));

        // TODO: handle exceptions and size mismatch
        var cid = Clojure.var("comlake-core.ipfs", "add").invoke(body);
        metadata.put("cid", cid);

        var insert = Clojure.var("comlake-core.rethink", "insert");
        insert.invoke(metadata);
        var json = gson.toJson(Map.of("cid", cid));
        return respond(200, contentType("application/json"), json);
    }

    /** Return query result as a http response. **/
    public static Map find(ByteArrayInputStream ast) {
        var reader = new InputStreamReader(ast);
        var parseAst = Clojure.var("comlake-core.rethink", "parse-qast");
        var query = parseAst.invoke(gson.fromJson(reader, List.class));
        if (query == null)
            return error("malformed query");

        var search = Clojure.var("comlake-core.rethink", "search");
        var body = gson.toJson(search.invoke(query));
        return respond(200, contentType("application/json"), body);
    }

    /**
     * Forward content from underlying distributed filesystem
     * as a Ring response.
    **/
    public static Map get(String cid) {
        var body = Clojure.var("comlake-core.ipfs", "fetch").invoke(cid);
        if (body == null)
            return error("content not found", 404);
        return respond(200, contentType("application/octet-stream"), body);
    }
}
