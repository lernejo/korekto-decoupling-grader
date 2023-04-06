package com.github.lernejo.korekto.grader.decoupling.parts;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvocationUtils {

    public static Object invokeMatchingConstructor(Class<?> clazz, Object... parameters) throws ReflectiveOperationException {
        Constructor<?>[] constructors = clazz.getConstructors();
        Optional<Constructor<?>> positionalConstructor = Arrays.stream(constructors)
            .filter(c -> c.getParameterCount() == parameters.length)
            .filter(c -> areParametersCompatible(c, parameters))
            .findFirst();
        if (positionalConstructor.isEmpty()) {
            Optional<Constructor<?>> arrayConstructor = Arrays.stream(constructors)
                .filter(c -> c.getParameterCount() == 1)
                .filter(c -> c.getParameterTypes()[0].isArray())
                .filter(c -> typesMatches(c.getParameterTypes()[0].componentType(), parameters))
                .findFirst();
            if (arrayConstructor.isEmpty()) {
                throw new NoSuchMethodException(clazz.getName() + "#<init>(" + Arrays.stream(parameters).map(Object::getClass).map(Class::getName).collect(Collectors.joining(", ")) + ')');
            } else {
                Constructor<?> constructor = arrayConstructor.get();
                Object arrayParameter = createProperArrayOfType(constructor.getParameterTypes()[0].componentType(), parameters);
                return constructor.newInstance(new Object[]{arrayParameter});
            }
        } else {
            return positionalConstructor.get().newInstance(getParametersInOrder(positionalConstructor.get(), parameters));
        }
    }

    private static Object createProperArrayOfType(Class<?> parameterType, Object[] parameters) {
        Object array = Array.newInstance(parameterType, parameters.length);
        for(var i = 0 ; i < parameters.length; i++) {
            Array.set(array, i, parameters[i]);
        }
        return array;
    }

    private static boolean typesMatches(Class<?> type, Object[] objects) {
        return Arrays.stream(objects).allMatch(o -> o == null || type.isAssignableFrom(o.getClass()));
    }

    private static Object[] getParametersInOrder(Constructor<?> constructor, Object[] parameters) {
        List<Object> parameterBag = new ArrayList<>(Arrays.asList(parameters));
        return Arrays.stream(constructor.getParameterTypes())
            .map(pt -> findMatchingParameter(pt, parameterBag))
            .map(Optional::get)
            .toArray();
    }

    private static boolean areParametersCompatible(Constructor<?> c, Object[] parameters) {
        List<Object> parameterBag = new ArrayList<>(Arrays.asList(parameters));
        return Arrays.stream(c.getParameterTypes())
            .allMatch(parameterType -> findMatchingParameter(parameterType, parameterBag).isPresent());
    }

    private static Optional<Object> findMatchingParameter(Class<?> parameterType, List<Object> parameterBag) {
        Optional<Object> match = parameterBag
            .stream()
            .filter(p -> parameterType.isAssignableFrom(p.getClass()))
            .findFirst();
        match.ifPresent(parameterBag::remove);
        return match;
    }
}
