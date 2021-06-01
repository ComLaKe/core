/*
 * Metadata class
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

package comlake_core.db;

import comlake_core.db.BaseMetadata;

/**
 * Metadata class, which is supposed to be read-only but Java does not have
 * an option better than the atrocity of private fields and getter methods.
**/
public class Metadata extends BaseMetadata {
    public String cid;

    public static Metadata of(BaseMetadata base, String contentID) {
        var result = new Metadata();
        result.length = base.length;
        result.type = base.type;
        result.name = base.name;
        result.source = base.source;
        result.topics = base.topics;
        result.optional = base.optional;
        result.cid = contentID;
        return result;
    }
}
