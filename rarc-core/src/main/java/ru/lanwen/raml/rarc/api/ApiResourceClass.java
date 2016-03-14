package ru.lanwen.raml.rarc.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.Resource;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ApiResourceClass {

    private String packageName;
    private String className;
    private Resource resource;

    private List<Field> fields = new ArrayList<>();
    private List<Method> methods = new ArrayList<>();
    private List<TypeSpec> enums = new ArrayList<>();

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

    public ApiResourceClass withEnum(TypeSpec en) {
        checkNotNull(en, "field==null");
        enums.add(en);
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

        List<AddParamMethod> addParamMethods = methods.stream()
                .filter(method -> method instanceof AddParamMethod)
                .map(method -> (AddParamMethod) method)
                .collect(groupingBy(AddParamMethod::name)).entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().collect(new FormQueryParamsMerge(this)).stream())
                .collect(toList());
        apiClass.addMethods(addParamMethods.stream().map(Method::methodSpec).collect(Collectors.toList()));
        apiClass.addMethods(methods.stream()
                .filter(method -> !(method instanceof AddParamMethod)).map(Method::methodSpec).collect(toList()));
        
        enums.forEach(apiClass::addType);

        return JavaFile.builder(basePackage + "." + packageName, apiClass.build()).build();
    }

    public static String packageName(Resource resource) {
        String packageName = sanitize(resource.getUri())
                .toLowerCase()
                .replace("//", "/")
                .replace("/", ".");

        Preconditions.checkArgument(SourceVersion.isName(packageName), "%s is wrong package name", packageName);
        return packageName;
    }


    public static String className(Resource resource) {
        return "Api" + classPart(resource.getParentResource()) + classPart(resource);
    }

    public static String sanitize(String string) {
        String underscoresFixed = Splitter.on("_").splitToList(string).stream().map(StringUtils::capitalize)
                .collect(joining());
        String identifier = uncapitalize(underscoresFixed)
                .replaceAll("[^A-Za-z1-9\\./]", "")
                .replaceAll("^([1-9]+)", "_$1")
                .replaceAll("^/", "")
                .replaceAll("/$", "");
        return identifier;
    }

    public static String classPart(Resource resource) {
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
