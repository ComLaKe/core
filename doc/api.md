# HTTP API Reference

## Status Codes

HTTP status codes used by the core API are simple:

* 200 OK: The request was processed or is being processed (streaming).
* 400 Bad Request: Malformed request was received.
* 404 Not Found: The endpoint does not exist.

## POST /add

Add the file along with its metadata to the data lake.

### Request

The entire request body shall be added as-is to the underlying storage.
The following headers are required:

* Content-Length: Size of the data in the body
* Content-Type: MIME type of the data
* X-Comlake-Name: Name of the data
* X-Comlake-Source: Original source of the data
* X-Comlake-Topics: Comma-separated list of classifiers

All other headers prefixed by `X-Comlake-` shall also be parsed as-is
as dynamic metadata.

#### Example

```http
POST /add HTTP/1.1
Accept: text/plain
Content-Length: 1284
Content-Type: text/plain
X-Comlake-Name: Interjection
X-Comlake-Source: https://wiki.installgentoo.com/index.php/Interjection
X-Comlake-Topics: Natural language,copypasta
X-Comlake-Language: English

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

The server respond with the content ID of the data.

#### Example

```http
HTTP/1.1 200 OK
Content-Type: text/plain
Content-Length: 46

QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5
```

## POST /find

## GET /get/<path>
