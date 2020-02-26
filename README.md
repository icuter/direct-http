Direct Http
=======

## Features
1. DSL Style Programming
2. Reusable Http Connection
3. Http Connections Pool
4. Async Request

### 1. DSL Style

```java
Response response = DirectHttp.newRequest("host")
    .encoding("utf-8")
    .contentType("application/json")
    .compression("gzip")
    .cookie(CookieObject... cookies)
    .header("name1", "value").header("name2", "value")
    .queryString("name1", "value").queryString("name2", "value")
    .formField("name1", "value").formField("name2", "value")
    .multipart("file", InputStream) // upload
    .multipart("partname", "text")
    .body("JSON/XML/FORM-FIELDS/ANY-OTHERS")
    .post("relative-path")
    .get("relative-path")
    .put("relative-path")
    .patch("relative-path")
    .send("method", "relative-path");

response.getBody().asJSON(); // compatiable with JSON lib
response.getBody().asString(); // same as toString()
response.getBody().asInputStream(); // download
response.getCookies(); // receive from http response header
response.getHeaders();
response.getStatusCode();

// close request
response.getRequest().close();
```

```java
try (Request request = DirectHttp.newRequest("host")) {
    Response response = request.body("JSON/XML/FORM-FIELDS/ANY-OTHERS").post("relative-path");

    response.getBody().asJSON(); // compatiable with JSON lib
    response.getBody().asString(); // same as toString()
    response.getBody().asInputStream(); // download
    response.getCookies(); // receive from http response header
    response.getHeaders();
    response.getStatusCode();
}
```

> host would be like that "http://uri" or "https://uri"

### 2. Reusable Http Connection

```java
// unsafe Request (NOT thread safe)
try (Request request = DirectHttp.newRequest("host")) {
    Response response = request.body("JSON/XML/FORM-FIELDS/ANY-OTHERS").post("relative-path");
    // ... response ...

    Response response2 = request.formField("name", "value").post("relative-path");
    // ... response2 ...

    assert response.getRequest() == response2.getRequest(); 
}
```

### 3. Http Connections Pool

```java
PooledRequest pool = DirectHttp.pooledRequest("host");
try (Request request = pool.getRequest()) {
    Response response = request.body("JSON/XML/FORM-FIELDS/ANY-OTHERS").post("relative-path");
    // ... response ...

    Response response2 = request.formField("name", "value").post("relative-path");
    // ... response2 ...

    assert response.getRequest() == response2.getRequest(); 
}
// pool.close(); close the pooled request immediately
```

Global http connections pool configuration 

```java
DirectHttp.globalPooledConfig().max(int).min(int).idle(TimeUnit).lifetime(TimeUnit);
```

### 4. Async Request

```java
PooledRequest pooled = DirectHttp.newPooledRequest("host");
try (Request request = pooled.getRequest()) {
    Future<Response> future1 = request.body("JSON/XML/FORM-FIELDS/ANY-OTHERS").asyncPost("relative-path", new Callback<Response>() {
        @Override
        public void process(Response resp) {
            // ... resp
        }
    });
    // ... future1 ...

    Future<Response> future2 = request.formField("name", "value").asyncSend("post", "relative-path", resp -> {
        // ... resp
    });
    // ... future2 ...
}
```