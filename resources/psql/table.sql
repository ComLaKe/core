DROP TABLE IF EXISTS comlake;
CREATE TABLE comlake (id serial primary key,
                      cid text,
                      length bigint,
                      type text,
                      name text,
                      source text,
                      topics text[],
                      optional json);
