# ComLake Core

USTH data lake core

## Prerequisites

* [Leiningen](https://leiningen.org)
* [RethinkDB](https://rethinkdb.com)
* [PostgreSQL](https://www.postgresql.org)
* [IPFS](https://ipfs.io)

## Hacking

    $ lein repl
    comlake-core.main=> (def server (-main)) ; start HTTP server
    comlake-core.main=> (.close server) ; stop server (e.g. to restart)

## Copying

![AGPLv3](https://www.gnu.org/graphics/agplv3-155x51.png)

This program is free software: you can redistribute them and/or modify them
under the terms of the GNU [Affero General Public License][agplv3] version 3
as published by the Free Software Foundation.

[agplv3]: https://www.gnu.org/licenses/agpl-3.0.html
