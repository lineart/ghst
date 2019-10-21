CREATE TABLE IF NOT EXISTS repo (
  id integer NOT NULL,  
  name varchar(100) NOT NULL,  
  full_name varchar(500) NOT NULL,  
  description varchar(1000),  
  url varchar(500) NOT NULL,  
  created_at timestamp,
  stargazers_count integer,
  PRIMARY KEY (id)  
);

CREATE INDEX idx_repo_stargazers_count
  ON repo( stargazers_count DESC NULLS LAST );

CREATE INDEX idx_repo_created_at
  ON repo( created_at DESC NULLS LAST );
