package dev.jsinco.brewery.util;

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
}
