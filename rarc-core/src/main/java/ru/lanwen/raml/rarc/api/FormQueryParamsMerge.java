package ru.lanwen.raml.rarc.api;

import com.google.common.collect.Sets;
import ru.lanwen.raml.rarc.api.ra.AddFormParamMethod;
import ru.lanwen.raml.rarc.api.ra.AddQueryParamMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

/**
 * @author lanwen (Merkushev Kirill)
 */
class FormQueryParamsMerge implements Collector<AddParamMethod, List<AddParamMethod>, List<AddParamMethod>> {
    private static final String FORM_PARAM_SUFFIX = "AsForm";
    private static final String QUERY_PARAM_SUFFIX = "AsQuery";
    private ApiResourceClass apiResourceClass;

    public FormQueryParamsMerge(ApiResourceClass apiResourceClass) {
        this.apiResourceClass = apiResourceClass;
    }

    @Override
    public Supplier<List<AddParamMethod>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<AddParamMethod>, AddParamMethod> accumulator() {
        return (list, elem) -> {
            
            // Clear duplicates
            List<AddParamMethod> collect = list.stream()
                    .filter(next -> elem.getClass().isInstance(next)).collect(toList());
            if (!collect.isEmpty()) {
                return;
            }

            if (elem instanceof AddQueryParamMethod) {
                Optional<AddParamMethod> form = list.stream().filter(item -> item instanceof AddFormParamMethod).findFirst();
                if (form.isPresent()) {
                    list.remove(form.get());
                    list.add(new AddFormParamMethod(((AddFormParamMethod) form.get()).getParam(), form.get().name(), ((AddFormParamMethod) form.get()).getReq(), apiResourceClass, FORM_PARAM_SUFFIX));
                    list.add(new AddQueryParamMethod(((AddQueryParamMethod) elem).getParam(), elem.name(), ((AddQueryParamMethod) elem).getReq(), apiResourceClass, QUERY_PARAM_SUFFIX));
                } else {
                    list.add(elem);
                }
                return;
            }

            if (elem instanceof AddFormParamMethod) {
                Optional<AddParamMethod> query = list.stream().filter(item -> item instanceof AddQueryParamMethod).findFirst();
                if (query.isPresent()) {
                    list.remove(query.get());
                    list.add(new AddQueryParamMethod(((AddQueryParamMethod) query.get()).getParam(), query.get().name(), ((AddQueryParamMethod) query.get()).getReq(), apiResourceClass, QUERY_PARAM_SUFFIX));
                    list.add(new AddFormParamMethod(((AddFormParamMethod) elem).getParam(), elem.name(), ((AddFormParamMethod) elem).getReq(), apiResourceClass, FORM_PARAM_SUFFIX));
                } else {
                    list.add(elem);
                }
            } else {
                list.add(elem);
            }
        };
    }

    @Override
    public BinaryOperator<List<AddParamMethod>> combiner() {
        return (list, list2) -> {
            throw new AbstractMethodError("Not implemented");
        };
    }

    @Override
    public Function<List<AddParamMethod>, List<AddParamMethod>> finisher() {
        return list -> list;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Sets.immutableEnumSet(Characteristics.UNORDERED);
    }
}
