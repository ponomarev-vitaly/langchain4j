package dev.langchain4j.agent.tool;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonSchemaHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.model.chat.request.json.JsonSchemaHelper.jsonSchemaElementFrom;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Utility methods for {@link ToolSpecification}s.
 */
public class ToolSpecifications {

    private ToolSpecifications() {
    }

    /**
     * Returns {@link ToolSpecification}s for all methods annotated with @{@link Tool} within the specified class.
     *
     * @param classWithTools the class.
     * @return the {@link ToolSpecification}s.
     */
    public static List<ToolSpecification> toolSpecificationsFrom(Class<?> classWithTools) {
        List<ToolSpecification> toolSpecifications = stream(classWithTools.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Tool.class))
                .map(ToolSpecifications::toolSpecificationFrom)
                .collect(toList());
        validateSpecifications(toolSpecifications);
        return toolSpecifications;
    }

    /**
     * Returns {@link ToolSpecification}s for all methods annotated with @{@link Tool}
     * within the class of the specified object.
     *
     * @param objectWithTools the object.
     * @return the {@link ToolSpecification}s.
     */
    public static List<ToolSpecification> toolSpecificationsFrom(Object objectWithTools) {
        return toolSpecificationsFrom(objectWithTools.getClass());
    }

    /**
     * Validates all the {@link ToolSpecification}s. The validation checks for duplicate method names.
     * Throws {@link IllegalArgumentException} if validation fails
     *
     * @param toolSpecifications list of ToolSpecification to be validated.
     */
    public static void validateSpecifications(List<ToolSpecification> toolSpecifications) throws IllegalArgumentException {

        // Checks for duplicates methods
        Set<String> names = new HashSet<>();
        for (ToolSpecification toolSpecification : toolSpecifications) {
            if (!names.add(toolSpecification.name())) {
                throw new IllegalArgumentException(format("Tool names must be unique. The tool '%s' appears several times", toolSpecification.name()));
            }
        }
    }

    /**
     * Returns the {@link ToolSpecification} for the given method annotated with @{@link Tool}.
     *
     * @param method the method.
     * @return the {@link ToolSpecification}.
     */
    public static ToolSpecification toolSpecificationFrom(Method method) {
        Tool annotation = method.getAnnotation(Tool.class);

        String name = isNullOrBlank(annotation.name()) ? method.getName() : annotation.name();

        String description = String.join("\n", annotation.value());
        if (description.isEmpty()) {
            description = null;
        }

        Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        Map<Class<?>, JsonSchemaHelper.VisitedClassMetadata> visited = new LinkedHashMap<>();

        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(ToolMemoryId.class)) {
                continue;
            }

            boolean isRequired = Optional.ofNullable(parameter.getAnnotation(P.class))
                    .map(P::required)
                    .orElse(true);

            properties.put(parameter.getName(), toJsonSchemaElement(parameter, visited));
            if (isRequired) {
                required.add(parameter.getName());
            }
        }

        Map<String, JsonSchemaElement> defs = new LinkedHashMap<>();
        visited.forEach((clazz, visitedClassMetadata) -> {
            if (visitedClassMetadata.recursion) {
                defs.put(visitedClassMetadata.ref, visitedClassMetadata.jsonSchemaElement);
            }
        });

        JsonObjectSchema parameters = JsonObjectSchema.builder()
                .properties(properties)
                .required(required)
                .defs(defs.isEmpty() ? null : defs) // TODO
                .build();

        if (properties.isEmpty()) {
            parameters = null; // TODO
        }

        return ToolSpecification.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .build();
    }

    private static JsonSchemaElement toJsonSchemaElement(Parameter parameter,
                                                         Map<Class<?>, JsonSchemaHelper.VisitedClassMetadata> visited) {
        P annotation = parameter.getAnnotation(P.class);
        String description = annotation == null ? null : annotation.value();
        return jsonSchemaElementFrom(parameter.getType(), parameter.getParameterizedType(), description, visited);
    }
}
