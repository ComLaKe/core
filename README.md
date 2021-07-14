# ComLake Core

USTH data lake core

## Prerequisites

[Leiningen](https://leiningen.org) is required for building the project.
[PostgreSQL](https://www.postgresql.org) and [IPFS](https://ipfs.io)
are runtime dependencies, whose access can be configured
in `$CONFIG_DIR/comlake/core.toml`, where `$CONFIG_DIR` is looked up
by [appdirs](https://github.com/harawata/appdirs)'s `getSiteConfigDir`
and `getUserConfigDir` in that order (which are respectively `/etc/xdg`
and `$HOME/.config` on XDG-compliant systems).  By default the configuration
is equivalent to the following:

```toml
ipfs-multiaddr = "/ip4/127.0.0.1/tcp/5001"
psql-url = "jdbc:postgresql://127.0.0.1:5432/comlake"
psql-user = "postgres"
psql-passwd = "postgres"
```

## Usage

While `lein run` in the project's root directory should suffice,
it is recommended to compile ahead of time for a better performance
(and independence from Leiningen):

    git clone https://github.com/ComLake/comlake.core
    cd comlake.core
    lein uberjar
    java -jar target/uberjar/comlake.core-*-standalone.jar

[Rendered documentation of the core API][doc] is hosted via GitHub pages.

## Hacking

    $ lein repl
    comlake.core.main=> (def server (-main)) ; start HTTP server
    comlake.core.main=> (.close server) ; stop server (e.g. to restart)

## Copying

![AGPLv3](https://www.gnu.org/graphics/agplv3-155x51.png)

This program is free software: you can redistribute them and/or modify them
under the terms of the GNU [Affero General Public License][agplv3] version 3
as published by the Free Software Foundation.

[doc]: https://comlake.github.io/comlake.core/api.html
[agplv3]: https://www.gnu.org/licenses/agpl-3.0.html
