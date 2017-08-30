# Cas security spring boot sample

[![asciicast](https://asciinema.org/a/eNoup6KEIfd2TbmSwnpQxdjCT.png)](https://asciinema.org/a/eNoup6KEIfd2TbmSwnpQxdjCT)

## Run using docker

```bash
docker-compose build --no-cache 
docker-compose up -d
```

**ATTENTION** first time usage, CAS Server will take many time to deploy (around ~5mins), you can check progress using `docker-compose logs -f`.

## Usage

Go to http://localhost:8081 and you should be redirected to http://localhost:8082 (CAS Server).

Use casuser/Mellon as login/password.
