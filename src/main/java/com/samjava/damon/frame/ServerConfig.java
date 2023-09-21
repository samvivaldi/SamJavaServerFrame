package com.samjava.damon.frame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ServerSocketFrame 설정을 위한 annotation
 * @author 20150721 sam 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerConfig {
	public int minThread();
	public int maxThread();
	public int timeout() default 3000;
	
}
