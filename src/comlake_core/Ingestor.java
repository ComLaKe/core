/*
 * Data and metadata ingestor
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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import comlake_core.BaseMetadata;
import comlake_core.Database;
import comlake_core.FileSystem;
import comlake_core.Metadata;

public class Ingestor {
    static final String[] requiredFields = {"length", "type",
                                            "name", "source", "topics"};

    private FileSystem fs;
    private Database db;

    public Ingestor(FileSystem filesystem, Database database) {
        fs = filesystem;
        db = database;
    }

    static Map<String, String> preprocess(Map<String, String> headers) {
        var result = new HashMap<String, String>();
        for (var header : headers.entrySet()) {
            var key = header.getKey();
            switch (key) {
            case "content-length":
            case "content-type":
                result.put(key.substring(8), header.getValue());
                break;
            case "x-comlake-length":
            case "x-comlake-type":
                break;  // disarm footgun
            default:
                if (key.startsWith("x-comlake-"))
                    result.put(key.substring(10), header.getValue());
            }
        }
        return result;
    }

    static Outcome<BaseMetadata, Map> parse(Map<String, String> raw) {
        // How could set difference not be in Java standard library?
        var missing = Arrays.stream(requiredFields)
            .filter(field -> raw.get(field) == null)
            .collect(Collectors.toList());
        if (!missing.isEmpty())
            return Outcome.fail(Map.of("missing-metadata", missing));

        var length = new BigInteger(raw.remove("length"));
        var type = raw.remove("type");
        var name = raw.remove("name");
        var source = raw.remove("source");
        // Should we trim whitespaces?
        var topics = raw.remove("topics").split("\\s*,\\s*");
        var base = new BaseMetadata(length, type, name, source, topics, raw);
        return Outcome.pass(base);
    }

    /**
     * Ingest data from the given request.
     *
     * Return the content ID upon success, otherwise the appropriate error.
     * Errors not due to given arguments are represented by null.
    **/
    public Outcome<String, Object> add(Map<String, String> headers,
                                       InputStream body) {
        var base = parse(preprocess(headers));
        if (!base.ok)
            return Outcome.fail(base.error);

        // TODO: handle size mismatch
        var cid = fs.add(body);
        if (cid == null)
            return Outcome.fail("empty data");
        if (!db.insert(Metadata.of(base.result, cid)))
            return Outcome.fail(null);
        return Outcome.pass(cid);
    }
}
