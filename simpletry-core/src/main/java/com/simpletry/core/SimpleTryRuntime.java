package com.simpletry.core;

public class SimpleTryRuntime {

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target) {

        if(target == null){
            throw new IllegalArgumentException("Target cannot be null");
        }

        try {

            Class<?> originalClass = target.getClass();

            String wrapperName =
                    originalClass.getName() + "SimpleTryWrapper";

            Class<?> wrapperClass = Class.forName(wrapperName);

            return (T) wrapperClass
                    .getConstructor(originalClass)
                    .newInstance(target);

        } catch (ClassNotFoundException e) {

            // No wrapper generated → return original object
            return target;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to initialize SimpleTry wrapper for "
                            + target.getClass().getName(),
                    e
            );
        }
    }
}