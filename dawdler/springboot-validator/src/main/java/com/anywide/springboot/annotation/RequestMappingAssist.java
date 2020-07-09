package com.anywide.springboot.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>RequestMappingAssist.java</p>
 * <p>Description: </p>
 * <p>Copyright: anywide groups 2013-4-7</p>
 * <p>Company: anywide </p>
 * @author srchen email:jackson.song@roiland.com
 * @date 2013-4-7 下午12:24:14
 * @version 1.0
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMappingAssist {
	String input() default "";
//	boolean hasJsonBody() default false;
//	ViewType viewType() default ViewType.jsonView;
	boolean generateValidator() default false;
}
