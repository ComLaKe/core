/*
 * File system interface
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

package comlake.core.fs;

import java.io.InputStream;
import java.util.Map;

public interface FileSystem {
    /** Create an empty directory and return the CID. **/
    public String mkdir();

    /**
     * Add the content of the given stream to underlying storage
     * and return the CID.
    **/
    public String add(InputStream istream);

    /** List the directory content if applicable, otherwise return nil. **/
    public Map<String, String> ls(String cid);

    /**
     * Stream the specified file if given valid content identifier,
     * otherwise return nil.
    **/
    public InputStream fetch(String cid);
}
