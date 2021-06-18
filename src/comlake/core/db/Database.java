/*
 * Database interface
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

package comlake.core.db;

import java.util.List;
import java.util.Map;

import comlake.core.db.Metadata;

public interface Database extends AutoCloseable {
    /** Clear the table. **/
    public boolean clear();

    /** Insert given row to underlying database. **/
    public boolean insert(Metadata metadata);

    /** Insert given file to table content. **/
    public boolean insertFile(String cid, String type);

    /** Insert given directory to table content. **/
    public boolean insertDirectory(String cid);

    /** Insert given row to table dataset. **/
    public String insertDataset(Map<String, Object> dataset);

    /** Insert updated row to table dataset. **/
    public String updateDataset(Map<String, Object> dataset);

    /** Filter for rows matching predicate, return null on errors. **/
    public List<Metadata> search(String predicate);
}
