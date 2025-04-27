package dev.jsinco.brewery.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCommandInfo {
    String name();

    String permission();

    String description() default "";

    String usage() default "";

    String[] aliases() default {};

    boolean isPlayerOnly() default false;

}
