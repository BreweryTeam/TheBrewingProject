package dev.jsinco.brewery.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class ClassUtil {

    private ClassUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean exists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean exists(String className, String methodName, Class<?>... parameterTypes) {
        try {
            Class<?> clazz = Class.forName(className);
            clazz.getMethod(methodName, parameterTypes);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }
    }

    public static <T> Optional<T> invoke(Object object, String methodName) {
        try {
            Class<?> clazz = object.getClass();
            Method method = clazz.getMethod(methodName);
            return Optional.of((T) method.invoke(object));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return Optional.empty();
        }
    }
}
