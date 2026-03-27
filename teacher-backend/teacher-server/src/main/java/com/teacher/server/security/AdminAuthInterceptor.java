package com.teacher.server.security;

import com.teacher.common.exception.BusinessException;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (path.startsWith("/api/admin")) {
            LoginUser loginUser = LoginUserContext.get();
            if (loginUser == null) {
                throw new BusinessException("请先登录管理员账号");
            }
            if (!loginUser.isAdmin()) {
                throw new BusinessException("管理员权限不足");
            }
        }
        return true;
    }
}
