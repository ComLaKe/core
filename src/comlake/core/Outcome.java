/*
 * Generic outcome type for replacing exceptions
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

package comlake.core;

/**
 * Generic outcome type to replace exceptions,
 * intended for read-only usage.
**/
public class Outcome<Result, Error> {
    /** Flag indicating outcome status. **/
    public boolean ok;

    /** OK outcome result. **/
    public Result result;

    /** Error outcome result. **/
    public Error error;

    public Outcome() { }

    private Outcome(boolean b, Result r, Error e) {
        // Allow wrapped value to be null.
        assert r == null || e == null;
        ok = b;
        result = r;
        error = e;
    }

    /** Create a OK outcome. **/
    public static <Result, Error> Outcome<Result, Error> pass(Result value) {
        return new Outcome<Result, Error>(true, value, null);
    }

    /** Create an error outcome. **/
    public static <Result, Error> Outcome<Result, Error> fail(Error value) {
        return new Outcome<Result, Error>(false, null, value);
    }
}
