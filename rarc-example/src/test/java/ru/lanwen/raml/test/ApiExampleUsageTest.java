package ru.lanwen.raml.test;

import io.restassured.builder.RequestSpecBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;

import static java.util.function.Function.identity;

/**
 * @author lanwen (Merkushev Kirill)
 */
@Ignore
public class ApiExampleUsageTest {

    @Test
    public void shouldBeAbleToCompile() {
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

    }

    @Test
    public void shouldUseDuplicateParams() {
        ApiExample.example(
                ApiExample.Config.exampleConfig()
                        .withReqSpecSupplier(
                                () -> new RequestSpecBuilder().setBaseUri("http://your_host/")
                        )
        ).hardDuplicate().withDuplicatedParam("blah").post(identity()).prettyPeek();
    }

    @Test
    public void shouldAcceptParamsOfAnyClass() {
        ApiExample.example(
                ApiExample.Config.exampleConfig()
                        .withReqSpecSupplier(
                                () -> new RequestSpecBuilder().setBaseUri("http://your_host/")
                        )
        ).passedAsObjectParams()
                .withObjectQueryParam(Instant.now())
                .withObjectFormParam(true)
                .withNullValueParam(null)
                .post(identity()).prettyPeek();
    }

}
