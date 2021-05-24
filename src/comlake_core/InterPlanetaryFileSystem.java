/*
 * IPFS wrapper
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
import java.io.IOException;

import io.ipfs.api.IPFS;
import static io.ipfs.api.NamedStreamable.InputStreamWrapper;
import static io.ipfs.multihash.Multihash.fromBase58;

import comlake_core.FileSystem;

public class InterPlanetaryFileSystem implements FileSystem {
    private IPFS ipfs;

    public InterPlanetaryFileSystem(String multiaddr) {
        ipfs = new IPFS(multiaddr);
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
