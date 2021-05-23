/*
 * PostgreSQL wrapper
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
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Map;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import comlake_core.Database;
import comlake_core.Metadata;

public class PostgreSQL implements Database {
    private static ComboPooledDataSource pool = new ComboPooledDataSource();

    static {
        try {
            pool.setDriverClass("org.postgresql.Driver");
            pool.setJdbcUrl("jdbc:postgresql:comlake");
            // FIXME: credentials should be securely stored
            pool.setUser("postgres");
            pool.setPassword("postgres");
        } catch (PropertyVetoException e) {
            // TODO: say something
        }
    }

    /** Insert given row to PostgreSQL. **/
    public void insert(Metadata metadata) {
        try {
            var conn = pool.getConnection();
            var sql = ("INSERT INTO comlake"
                       + " (cid, length, type, name, source, optional)"
                       + " VALUES (?, ?, ?, ?, ?, ?::json)");
            var statement = conn.prepareStatement(sql);
            statement.setObject(1, metadata.cid);
            statement.setObject(2, metadata.length);
            statement.setObject(3, metadata.type);
            statement.setObject(4, metadata.name);
            statement.setObject(5, metadata.source);
            statement.setObject(6, metadata.optional);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            // TODO: say something
        }
    }
}
