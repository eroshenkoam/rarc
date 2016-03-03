package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.Resource;

import static javax.lang.model.element.Modifier.PUBLIC;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.className;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.classPart;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.packageName;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class NextResourceMethods {
    
    public static MethodSpec baseResource(Resource resource, String basePackage, String confFName, String reqSupplFName) {
        return MethodSpec.methodBuilder(uncapitalize(substringAfter(className(resource), "Api")))
                .returns(ClassName.get(basePackage + "." + packageName(resource), className(resource)))
                .addStatement("return new $N($N.$N.get())", className(resource), confFName, reqSupplFName)
                .addModifiers(PUBLIC)
                .build();
    }
 
    public static MethodSpec childResource(Resource resource, String basePackage, String reqSpecFName) {
        String methodName = uncapitalize(isNotEmpty(resource.getDisplayName()) 
                ? classPart(resource) 
                : substringAfter(className(resource), "Api"));
        
        return MethodSpec.methodBuilder(methodName)
                .returns(ClassName.get(basePackage + "." + packageName(resource), className(resource)))
                .addStatement("return new $N($N)", className(resource), reqSpecFName)
                .addModifiers(PUBLIC)
                .build();
    }
}
