package pl.netbulls.mongounit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation loading fixtures (json files) to actually executed test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Fixture
{
	/**
	 * List of files with data to load.
	 *
	 * @return list of files.
	 */
	String[] value() default {};
}
