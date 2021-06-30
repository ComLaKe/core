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
                      extra jsonb,
                      parent bigint,
                      FOREIGN KEY (parent) REFERENCES dataset(id));
