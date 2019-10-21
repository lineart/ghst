# ghst
GitHub Statistics App

## Prerequisites

- [Docker](https://www.docker.com/) is used to build and run the App
- Any GraphQL explorer to query data

## Quick Start

### Build & Run
1. Clone repo: `git clone https://github.com/lineart/ghst.git`
2. Change workdir: `cd ghst`
3. Deploy: `docker-compose build`

### Verify
A GraphQL service (**Webapp**) runs at http://localhost:8888/graphql

At first run a **Crawler** service will query GitHub API to fill the database.
It takes some time, so it's a good idea to make a cup of coffee before first data will start to apear.

*NOTE:* Data is completely updated when Crawler service stops.

#### Querying with curl:
```
curl -X POST -H "Content-Type: application/json" \
        --data '{ "query": "{ reposTrending { name, stargazersCount } }" }'\
        http://localhost:8888/graphql
```
Example Response:
```
{
    "data": {
        "reposTrending": [
            {
                "name": "docker-nginx-http3",
                "stargazersCount": 441
            },
            ...
        }
    }
}
```

## App overview
```
     .-,(  ),-.    
  .-(          )-.         +---------+
(      GitHub     )<-------| Crawler |
  '-(          ).-'        +---------+
     '-.(  ).-'                 | REST
                                v              .----.
        +--------+  REST +--------------+ JPA |  --  |
 GQL >  | WebApp |<------| Data Service |<===>|RDBMS |
        +--------+       +--------------+      .____.

```

The App has three main services:
- [Data Service](ghst-service) - provides access to Database
- [Crawler](ghst-crawler) - queries GitHub API and fills the Database using *Data Service*
- [Webapp](ghst-webapp) - talks to *Data Service* and serves data using graphql API

Services are simple Java/Spring based applications that are built and running inside of docker

An official PostgreSQL container is used as RDBMS
