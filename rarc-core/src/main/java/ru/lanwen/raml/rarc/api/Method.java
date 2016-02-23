package ru.lanwen.raml.rarc.api;

import com.squareup.javapoet.MethodSpec;

/**
 * @author lanwen (Merkushev Kirill)
 */
@FunctionalInterface
public interface Method {
    MethodSpec methodSpec();
}
