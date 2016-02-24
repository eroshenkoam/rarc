package ru.lanwen.raml.rarc.api;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Resource;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ApiResourceClass {

    private String packageName;
    private String className;
    private Resource resource;

    private List<Field> fields = new ArrayList<>();
    private List<Method> methods = new ArrayList<>();

    public static ApiResourceClass forResource(Resource resource) {
        ApiResourceClass apiClass = new ApiResourceClass();
        apiClass.resource = resource;
        apiClass.packageName = packageName(resource);
        apiClass.className = className(resource);
        return apiClass;
    }

    public ApiResourceClass withMethod(Method method) {
        checkNotNull(method, "method==null");
        methods.add(method);
        return this;
    }

    public ApiResourceClass withField(Field field) {
        checkNotNull(field, "field==null");
        fields.add(field);
        return this;
    }

    public String name() {
        return className;
    }

    public JavaFile javaFile(String basePackage) {
        TypeSpec.Builder apiClass = TypeSpec.classBuilder(className)
                .addJavadoc("$L\n", trimToEmpty(resource.getDescription()))
                .addModifiers(Modifier.PUBLIC);
        
        apiClass.addFields(fields.stream().map(Field::fieldSpec).collect(toList()));
        apiClass.addMethods(methods.stream().map(Method::methodSpec).collect(toList()));

        return JavaFile.builder(basePackage + "." + packageName, apiClass.build()).build();
    }

    public static String packageName(Resource resource) {
        String packageName = sanitize(resource.getUri())
                .toLowerCase()
                .replace("/", ".");

        Preconditions.checkArgument(SourceVersion.isName(packageName), "%s is wrong package name", packageName);
        return packageName;
    }


    public static String className(Resource resource) {
        return "Api" + classPart(resource.getParentResource()) + classPart(resource);
    }

    public static String sanitize(String string) {
        return string
                .replaceAll("[^A-Za-z\\./]", "")
                .replaceAll("^/", "")
                .replaceAll("/$", "");
    }

    private static String classPart(Resource resource) {
        if (resource == null) {
            return "";
        }

        String name = isNotEmpty(resource.getDisplayName())
                ? sanitize(resource.getDisplayName())
                : sanitize(resource.getRelativeUri())
                .replace(".", "");

        return capitalize(name.contains("/") ? substringAfterLast(name, "/") : name);
    }
}
