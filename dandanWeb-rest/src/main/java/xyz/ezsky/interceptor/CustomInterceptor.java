package xyz.ezsky.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import xyz.ezsky.entity.dto.AppConfigDTO;
import xyz.ezsky.tasks.VideoScanner;

// TODO: 2024/4/27 备用 初始化的时候写 
@Component
public class CustomInterceptor implements HandlerInterceptor {

    @Autowired
    private AppConfigDTO appConfigDTO;
    @Autowired
    private VideoScanner videoScanner;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        return true; // 如果返回false，请求将被终止
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
