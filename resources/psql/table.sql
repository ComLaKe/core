DROP TABLE IF EXISTS comlake;
CREATE TABLE comlake (id serial primary key,
                      cid text,
                      length bigint,
                      type text,
                      name text,
                      source text,
                      topics text[],
                      optional json);
DROP TABLE IF EXISTS content CASCADE;
CREATE TABLE content (cid text PRIMARY KEY,
                      type text,
                      extra jsonb);
DROP TABLE IF EXISTS dataset;
CREATE TABLE dataset (id bigserial PRIMARY KEY,
                      file text REFERENCES content,
                      description text,
                      source text,
                      topics text[],
                      extra json,
                      parent bigint,
                      FOREIGN KEY (parent) REFERENCES dataset(id));
