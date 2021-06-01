/*
 * Database interface
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

import java.util.List;

import comlake_core.db.Metadata;

public interface Database extends AutoCloseable {
    /** Clear the table. **/
    public boolean clear();

    /** Insert given row to underlying database. **/
    public boolean insert(Metadata metadata);

    /** Filter for rows matching predicate, return null on errors. **/
    public List<Metadata> search(String predicate);
}
