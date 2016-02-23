package ru.lanwen.raml.rarc.api;

import com.squareup.javapoet.FieldSpec;

/**
 * @author lanwen (Merkushev Kirill)
 */
public interface Field {
    String name();

    FieldSpec fieldSpec();
}
