DROP TABLE IF EXISTS comlake;
CREATE TABLE comlake (id serial primary key,
                      cid text,
                      length bigint,
                      type text,
                      name text,
                      source text,
                      topics text[],
                      optional json);
DROP TABLE IF EXISTS files;
CREATE TABLE files (id bigserial PRIMARY KEY,
                    cid text,
                    length bigint,
                    type text,
                    name text,
                    info json);
DROP TABLE IF EXISTS entries;
CREATE TABLE entries (id bigserial PRIMARY KEY,
                      parent bigint REFERENCES entries,
                      file bigint REFERENCES files,
                      description text,
                      source text,
                      topics text[],
                      optional json);
