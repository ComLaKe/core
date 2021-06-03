/*
 * Configuration reader
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import com.github.jezza.Toml;
import com.github.jezza.TomlTable;

import net.harawata.appdirs.AppDirsFactory;

/** Configuration parsed from TOML, intended for read-only use. **/
public class Configuration {
    private static final String pathsep = System.getProperty("path.separator");
    private static final String cfg = pathsep + "core.toml";
    private static final String fallback = String.join(
        System.getProperty("line.separator"),
        "ipfs-multiaddr = \"/ip4/127.0.0.1/tcp/5001\"",
        "psql-url = \"jdbc:postgresql:comlake\"",
        "psql-user = \"postgres\"",
        "psql-passwd = \"postgres\"");

    private static TomlTable parse(File siteCfg, File userCfg) {
        var base = new StringReader(fallback);
        try {
            switch ((siteCfg.isFile() ? 1 : 0) | (userCfg.isFile() ? 2 : 0)) {
            case 1:
                return Toml.from(base, new FileReader(siteCfg));
            case 2:
                return Toml.from(base, new FileReader(userCfg));
            case 3:
                return Toml.from(base, new FileReader(siteCfg),
                                 new FileReader(userCfg));
            }
        } catch (FileNotFoundException e) {
            return null;  // unreachable
        } catch (IOException e) {
            // Fallback silently
        }

        try {
            return Toml.from(base);
        } catch (IOException e) {
            return null;  // unreachable
        }
    }

    public String ipfsMultiAddr;
    public String psqlUrl;
    public String psqlUser;
    public String psqlPasswd;

    public Configuration() {
        var appdirs = AppDirsFactory.getInstance();
        var siteCfg = appdirs.getUserConfigDir("comlake", null, null) + cfg;
        var userCfg = appdirs.getUserConfigDir("comlake", null, null) + cfg;
        var cfg = parse(new File(siteCfg), new File(userCfg));
        ipfsMultiAddr = (String) cfg.get("ipfs-multiaddr");
        psqlUrl = (String) cfg.get("psql-url");
        psqlUser = (String) cfg.get("psql-user");
        psqlPasswd = (String) cfg.get("psql-passwd");
    }
}
