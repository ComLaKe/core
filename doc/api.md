# HTTP API Reference

Table of content:

* [Status Codes](#status-codes)
* [POST /dir](#post-dir)
* [POST /file](#post-file)
* [POST /cp](#post-cp)
* [POST /dataset](#post-dataset)
* [POST /update](#post-update)
* [POST /find](#post-find)
* [GET /dir/{cid}](#get-dir-cid-)
* [GET /file/{cid}](#get-file-cid-)

## Status Codes

HTTP status codes used by the core API are simple:

* 200 OK: The request was processed or is being processed (streaming).
* 400 Bad Request: Malformed request was received.
* 404 Not Found: The endpoint does not exist.

## POST /dir

Create an empty directory.

### Request

No input is required.

#### Example

```http
POST /dir
```

### Response

The call to this endpoint will return a body in JSON containing either the `cid`
(content ID) of the data on success or the `error` message upon errors.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"cid":"QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn"}
```

## POST /file

Add the file to the underlying file system.

### Request

The entire request body shall be added as-is to the underlying storage.
The following headers are required:

* Content-Length: Size of the data in the body
* Content-Type: MIME type of the data

#### Example

```http
POST /file HTTP/1.1
Accept: application/json
Content-Length: 1284
Content-Type: text/plain

I'd just like to interject for a moment.  What you're referring to as Linux,
is in fact, GNU/Linux, or as I've recently taken to calling it, GNU plus Linux.
Linux is not an operating system unto itself, but rather another free component
of a fully functioning GNU system made useful by the GNU corelibs, shell
utilities and vital system components comprising a full OS as defined by POSIX.

Many computer users run a modified version of the GNU system every day,
without realizing it.  Through a peculiar turn of events, the version of GNU
which is widely used today is often called "Linux", and many of its users are
not aware that it is basically the GNU system, developed by the GNU Project.

There really is a Linux, and these people are using it, but it is just a
part of the system they use.  Linux is the kernel: the program in the system
that allocates the machine's resources to the other programs that you run.
The kernel is an essential part of an operating system, but useless by itself;
it can only function in the context of a complete operating system.  Linux is
normally used in combination with the GNU operating system: the whole system
is basically GNU with Linux added, or GNU/Linux.  All the so-called "Linux"
distributions are really distributions of GNU/Linux.
```

### Response

The call to this endpoint will return a body in JSON containing either the `cid`
(content ID) of the data on success or the `error` message upon errors.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"cid":"QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5"}
```

## POST /cp

Copy file or directory inside a directory.

### Request

The request body must be a JSON object with the following fields:

* `src`: CID of source file or directory
* `dest`: CID of destination directory
* `path`: relative path inside of `dest`

#### Example

Copy `QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5`
to `QmYwAPJzv5CZsnA625s3Xf2nemtYgPpHdWEz79ojWnPbdG/interjection`:

```http
POST /find HTTP/1.1
Accept: application/json
Content-Type: application/json

{"src": "QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5",
 "dest": "QmYwAPJzv5CZsnA625s3Xf2nemtYgPpHdWEz79ojWnPbdG",
 "path": "interjection"}
```

### Response

The call to this endpoint will return a body in JSON containing either the `cid`
of the resulting directory on success or the `error` message upon errors.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"cid":"QmPao7zTNvuqH2pAVUgquYXgEhqoiTBpdjU7AwgZvsta9r"}
```

## POST /dataset

Add the dataset to the lake.

### Request

The request body must be a JSON object.
The following fields are required:

* `file`: ID of the data file or directory
* `description`: Breif description of the datset
* `source`: Preferably an URI to the original dataset
* `topics`: Array of keywords related to the dataset

Extra fields will be ingested and used for later indexing.

#### Example

```http
POST /dataset HTTP/1.1
Accept: application/json
Content-Type: application/json
{"file": "QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5",
 "description": "Interjection",
 "source": "https://wiki.installgentoo.com/index.php/Interjection",
 "topics": ["Natural language", "copypasta"]}
```

### Response

The call to this endpoint will return a body in JSON containing either
the automatically generated dataset `id` on success or the `error` message
upon errors.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"id":"42"}
```

## POST /update

Add the updated dataset to the lake.

### Request

The request body must be a JSON object containing the field `parent`.
Other fields with be merged with the parent dataset entry.

#### Example

```http
POST /dataset HTTP/1.1
Accept: application/json
Content-Type: application/json
{"parent": "42", "language": "English"}
```

### Response

The call to this endpoint will return a body in JSON containing either
the automatically generated dataset `id` on success or the `error` message
upon errors.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"id":"69"}
```

## POST /find

Find the data according to the given predicate.

### Request

The predicate must be a valid [query AST](qast.md), represented in JSON.

#### Example

Find all data smaller than 4 KiB:

```http
POST /find HTTP/1.1
Accept: application/json
Content-Type: application/json

["<", [".", "length"], 4096]
```

### Response

The server must respond in JSON with an array of objects,
each representing a datum.  In case of an error, the response
would be an JSON object with an `error` field.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

[{"cid":"QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5","id":"589836fe-c2f7-4d21-a521-688439bc74a4","language":"English","length":1284,"name":"Interjection","source":"https:\/\/wiki.installgentoo.com\/index.php\/Interjection","topics":["Natural language","copypasta"],"type":"application\/x-www-form-urlencoded"}]
```

## GET /dir/{cid}

List content of a file system directory.

### Request

In the URI, `cid` specifies the content identifier of the wanted file.

#### Example

List the directory of CID `QmSnuWmxptJZdLJpKRarxBMS2Ju2oANVrgbr2xWbie9b2D`:

```http
GET /dir/QmSnuWmxptJZdLJpKRarxBMS2Ju2oANVrgbr2xWbie9b2D
```

### Response

The server must respond with a JSON object of `name: cid` upon success
or the `error` message in case of an error.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"albums":"QmUh6QSTxDKX5qoNU1GoogbhTveQQV9JMeQjfFVchAtd5Q","README.txt":"QmP8jTG1m9GSDJLCbeWhVSVgEzCPPwXRdCRuJtQ5Tz9Kc9","build_frontend_index.py":"QmRSxRRu9AoJ23bxb2pFeoAUFXMAdki7RZu2T7e6zHRdu6","_Metadata.json":"QmWXShtJXt6Mw3FH7hVCQvR56xPcaEtSj4YFSGjp2QxA4v","apolloarchivr.py":"QmU7gJi6Bz3jrvbuVfB7zzXStLJrTHf6vWh8ZqkCsTGoRC","frontend":"QmeQtZfwuq6aWRarY9P3L9MWhZ6QTonDe9ahWECGBZjyEJ"}
```

## GET /file/{cid}

Stream content from underlying file system.

### Request

In the URI, `cid` specifies the content identifier of the wanted file.

#### Example

Stream the file of content ID `QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5`:

```http
GET /file/QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5
```

### Response

The server should respond with a octet stream, transferred in chunks.
In case of an error, the response would be an JSON object with field `error`.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: application/octet-stream

I'd just like to interject for a moment.  What you're referring to as Linux,
is in fact, GNU/Linux, or as I've recently taken to calling it, GNU plus Linux.
Linux is not an operating system unto itself, but rather another free component
of a fully functioning GNU system made useful by the GNU corelibs, shell
utilities and vital system components comprising a full OS as defined by POSIX.

Many computer users run a modified version of the GNU system every day,
without realizing it.  Through a peculiar turn of events, the version of GNU
which is widely used today is often called "Linux", and many of its users are
not aware that it is basically the GNU system, developed by the GNU Project.

There really is a Linux, and these people are using it, but it is just a
part of the system they use.  Linux is the kernel: the program in the system
that allocates the machine's resources to the other programs that you run.
The kernel is an essential part of an operating system, but useless by itself;
it can only function in the context of a complete operating system.  Linux is
normally used in combination with the GNU operating system: the whole system
is basically GNU with Linux added, or GNU/Linux.  All the so-called "Linux"
distributions are really distributions of GNU/Linux.
```
