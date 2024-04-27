package xyz.ezsky.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.ezsky.interceptor.CustomInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CustomInterceptor customInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器并指定拦截路径
        registry.addInterceptor(customInterceptor).addPathPatterns("/**");
    }
}
