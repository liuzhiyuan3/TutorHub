package com.teacher.server.security;

import com.teacher.common.exception.BusinessException;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin")) {
            LoginUser loginUser = LoginUserContext.get();
            if (loginUser == null || !loginUser.isAdmin()) {
                throw new BusinessException("管理员权限不足");
            }
        }
        return true;
    }
}
