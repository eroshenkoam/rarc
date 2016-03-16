package ru.lanwen.raml.rarc.example;

import com.jayway.restassured.builder.RequestSpecBuilder;
import ru.lanwen.raml.test.ApiExample;

/**
 * Created by stassiak
 */
public class ExampleApi {
    public static ApiExample example() {
        return ApiExample.example(ApiExample.Config.exampleConfig()
                .withReqSpecSupplier(() ->
                        new RequestSpecBuilder()
                                .setRelaxedHTTPSValidation()
                                .setBaseUri("https://example.com")));
    }
}
