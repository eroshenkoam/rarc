# RARC - Rest-Assured RAML Codegen

This project simplifies generation of test clients 
(with [Rest-Assured lib](https://github.com/rest-assured/rest-assured/) under the hood) by your RAML spec.
Currently this project pointed to [0.8](http://raml.org/raml-08-spec), but 
will be upgraded to [1.0](http://raml.org/raml-10-spec) soon.

## Quick Start

- Place your spec to `src/main/resources/api.raml`
- Add to your `<build>` section in `pom.xml`: 

```xml
<plugin>
    <groupId>ru.lanwen.raml</groupId>
    <artifactId>rarc-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>generate-client</goal>
            </goals>
            <configuration>
                <basePackage>ru.lanwen.raml.test</basePackage>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- Add dependency to rest-assured (currently tested on `2.8.0`): 

```xml
<dependency>
    <groupId>com.jayway.restassured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>2.8.0</version>
</dependency>
```

- Run `mvn clean compile`
- Use it! (Don't forget to add static imports and factory to base endpoint)

```java
ApiExample.example(
                ApiExample.Config.exampleConfig()
                        .withReqSpecSupplier(
                                () -> new RequestSpecBuilder().setBaseUri("http://your_host/")
                        )
        )
                .rpcApi()
                .uid().withUid("1")
                .info()
                .get(identity()).prettyPeek();
```

See working example in `rarc-example` module.
