package ru.lanwen.raml.rarc.api.ra.root;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import ru.lanwen.raml.rarc.api.Field;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static ru.lanwen.raml.rarc.api.ra.root.NestedConfigClass.CONFIG_NESTED_STATIC_CLASS_NAME;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ConfigField implements Field {
    @Override
    public String name() {
        return "config";
    }

    @Override
    public FieldSpec fieldSpec() {
        return FieldSpec.builder(ClassName.bestGuess(CONFIG_NESTED_STATIC_CLASS_NAME), name(), PRIVATE, FINAL).build();
    }
}
