package ormx.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnInfo {
  
  String name() default ""; 
  
  boolean primaryKey() default false;
  
  boolean autoIncrement() default false;
}
