/*
 * IPFS wrapper
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
import java.io.IOException;
import java.util.Map;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;

import io.ipfs.api.IPFS;
import static io.ipfs.api.NamedStreamable.InputStreamWrapper;
import static io.ipfs.multihash.Multihash.fromBase58;

import comlake.core.fs.FileSystem;

public class InterPlanetaryFileSystem implements FileSystem {
    private IPFS ipfs;

    public InterPlanetaryFileSystem(String multiaddr) {
        ipfs = new IPFS(multiaddr);
    }

    /** Create an empty directory and return the CID. **/
    public String mkdir() {
        return "QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn";
    }

    /** Add the content of the given stream to IPFS and return the CID. **/
    public String add(InputStream istream) {
        var w = new InputStreamWrapper(istream);
        try {
            return ipfs.add(w).get(0).hash.toString();
        } catch (NullPointerException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /** Retrieve content status. **/
    private Map<String, String> stat(String path) {
        try {
            // TODO: Implement this in the IPFS client library
            return ipfs.retrieveMap("files/stat?arg="+path);
        } catch (RuntimeException e) {
            return null; // invalid CID
        } catch (IOException e) {
            return null;
        }
    }

    /** Check if the given CID is of a directory. **/
    private Boolean isdir(String path) {
        var status = stat(path);
        return status != null && status.get("Type").equals("directory");
    }

    /**
     * Copy src into dest/path and return the new directory's CID.
     *
     * Return null if dest isn't a directory.
    **/
    public String cp(String src, String dest, String path) {
        if (!isdir("/ipfs/"+dest))
            return null;

        var tmp = "/" + randomUUID().toString();
        try {
            // TODO: Implement this in the IPFS client library
            ipfs.retrieve("files/cp?arg=/ipfs/"+dest+"&arg="+tmp);
            ipfs.retrieve("files/cp?arg=/ipfs/"+src+"&arg="+tmp+"/"+path);
            var cid = stat(tmp).get("Hash");
            ipfs.pin.add(fromBase58(cid));
            ipfs.retrieve("files/rm?arg="+tmp+"&force=true");
            return cid;
        } catch (IOException e) {
            return null;
        }
    }

    /** List the directory content if applicable, otherwise return nil. **/
    public Map<String, String> ls(String cid) {
        if (!isdir("/ipfs/"+cid))
            return null;

        try {
            return ipfs.ls(fromBase58(cid)).stream().collect(toMap(
                node -> node.name.get(), node -> node.hash.toString()));
        } catch (IOException e) {
            return null;
        }
    }

    /** Stream the IPFS file if given CIDv0 is valid, otherwise return nil. **/
    public InputStream fetch(String cid) {
        try {
            return ipfs.catStream(fromBase58(cid));
        } catch (IllegalStateException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
