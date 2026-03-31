package com.oran.oranpicturebackend.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * 该注解只能应用于方法
 * */
@Target(ElementType.METHOD)
//该注解将被保留在编译后的类文件中，并且可以在运行时通过反射访问。
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /*
     * 必须有某个角色
     * */
    String mustRole() default "";
}
