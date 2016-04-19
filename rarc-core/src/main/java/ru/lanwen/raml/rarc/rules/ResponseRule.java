package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.Response;

/**
 * Created by stassiak
 */
public class ResponseRule implements Rule<Response, JavaFile>{
    @Override
    public JavaFile apply(Response generatableType) {
        return null;
    }
}
