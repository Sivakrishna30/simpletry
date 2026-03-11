package com.simpletry.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SimpleTry {

    Class<? extends Throwable>[] exceptions() default {Exception.class};

    boolean log() default false;

    String[] tag() default {};

    String[] fallbackValue() default {};

    String fallbackMethod() default "";

    Class<? extends Throwable>[] ignore() default {};

    Class<? extends Throwable> transformTo() default Throwable.class;

    int retry() default 0;

    boolean debugTrace() default false;
}