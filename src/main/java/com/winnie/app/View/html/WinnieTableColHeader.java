package com.winnie.app.View.html;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WinnieTableColHeader {
    String header();

    String dateFormat() default "";

    String numberFormat() default "";
}
