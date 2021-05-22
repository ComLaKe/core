/*
 * Metadata without content address
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
import java.util.Map;
import java.util.Set;

import comlake_core.Outcome;

/**
 * Metadata class, which is supposed to be read-only but Java does not have
 * an option better than the atrocity of private fields and getter methods.
**/
public class BaseMetadata {
    public BigInteger length;
    public String type;
    public String name;
    public String source;
    public String[] topics;
    public Map<String, String> optional;

    public BaseMetadata() {}

    public BaseMetadata(BigInteger length, String type, String name,
                        String source, String[] topics,
                        Map<String, String> optional) {
        this.length = length;
        this.type = type;
        this.name = name;
        this.source = source;
        this.topics = topics;
        this.optional = optional;
    }
}
