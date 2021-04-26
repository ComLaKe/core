# Query Abstract Syntax Tree

The query abstract syntax tree (query AST) is constructed from a minimal set
of operators and a syntax similar to [s-expressions][sexpr] but in JSON arrays
for portability on the web platform.  A valid expression is either a value
(number, string, boolean or object) or an operation denoted as an array
whose first element is an operator and the rest are operands.
The following operators are currently supported:

| Operator | Operands    | Result      | Description                         |
| :------: | :---------  | :---------- | :---------------------------------- |
|   `.`    | 1 string    | value       | Get field from current row          |
|   `~`    | 2 strings   | null/object | Regular expression matcher          |
|   `+`    | 1+ value    | value       | Sum or strings/arrays concatenation |
|   `-`    | 1+ numbers  | number      | Subtraction                         |
|   `*`    | 1+ numbers  | number      | Multiplication                      |
|   `/`    | 1+ numbers  | number      | Division                            |
|   `%`    | 2 numbers   | number      | Modulo                              |
|   `&`    | 0+ booleans | value       | Logical *and*                       |
|   `|`    | 0+ booleans | value       | Logical *or*                        |
|   `==`   | 2+ values   | boolean     | Equality                            |
|   `!=`   | 2+ values   | boolean     | Inequality                          |
|   `>`    | 2+ values   | boolean     | *Greater than* comparison           |
|   `>=`   | 2+ values   | boolean     | *Greater or equal* comparison       |
|   `<`    | 2+ values   | boolean     | *Less than* comparison              |
|   `<=`   | 2+ values   | boolean     | *Less or equal* comparison          |
|   `!`    | 1 boolean   | boolean     | Logical *not*                       |

Behaviors involving JSON `null` is deliberately left undefined.
The reference implementation considers queries with operations
containing `null`'s as malformed, but does not complain about
the appearance of `null` elsewhere; however API users must **not**
rely on this.

[sexpr]: https://en.wikipedia.org/wiki/S-expression
