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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Database extends AutoCloseable {
    /** Insert given file to table content. **/
    public boolean insertFile(String cid, String type);

    /** Insert given directory to table content. **/
    public boolean insertDirectory(String cid);

    /** Insert given row to table dataset. **/
    public String insertDataset(Map<String, Object> dataset);

    /** Insert updated row to table dataset. **/
    public String updateDataset(Map<String, Object> dataset);

    /** Filter for rows matching predicate, return null on errors. **/
    public ArrayList<Map<String, Object>> search(String predicate);

    /** Return content type. **/
    public String getType(String cid);

    /** Return schema of given (semi-)structured content. **/
    public String getSchema(String cid);

    /** Update schema of given (semi-)structured content. **/
    public void setSchema(String cid, String schema);

}
