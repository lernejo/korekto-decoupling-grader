package com.github.lernejo.korekto.grader.decoupling.parts;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvocationUtils {

    public static Object invokeMatchingConstructor(Class<?> clazz, Object... parameters) throws ReflectiveOperationException {
        Constructor<?>[] constructors = clazz.getConstructors();
        Optional<Constructor<?>> constructor = Arrays.stream(constructors)
            .filter(c -> c.getParameterCount() == parameters.length)
            .filter(c -> areParametersCompatible(c, parameters))
            .findFirst();
        if (constructor.isEmpty()) {
            throw new NoSuchMethodException(clazz.getName() + "#<init>(" + Arrays.stream(parameters).map(Object::getClass).map(Class::getName).collect(Collectors.joining(", ")) + ')');
        }
        return constructor.get().newInstance(getParametersInOrder(constructor.get(), parameters));
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
        if (match.isPresent()) {
            parameterBag.remove(match.get());
        }
        return match;
    }
}
