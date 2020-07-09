package com.anywide.springboot.interceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class ValidateAdapter implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加拦截器
//        registry.addInterceptor(new ValidateInterceptorForPTTL())
//                .addPathPatterns("/**");
    	 registry.addInterceptor(new ValidateInterceptor())
         .addPathPatterns("/**");
//        super.addInterceptors(registry);
    }
}
