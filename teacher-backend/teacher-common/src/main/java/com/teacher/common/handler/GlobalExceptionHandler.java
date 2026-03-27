package com.teacher.common.handler;

import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "数据约束校验失败";
        }
        String lower = msg.toLowerCase();
        if (lower.contains("uk_user_account") || lower.contains("user_account")) {
            return ApiResponse.fail("账号已存在");
        }
        if (lower.contains("uk_user_phone") || lower.contains("user_phone")) {
            return ApiResponse.fail("手机号已存在");
        }
        log.error("Data integrity violation: {}", msg, ex);
        return ApiResponse.fail("数据约束异常: " + msg);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatus(ResponseStatusException ex) {
        String msg = ex.getReason();
        if (msg == null || msg.isBlank()) {
            msg = "请求失败";
        }
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>(ex.getStatusCode().value(), msg, null));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        String msg = ex.getMessage();
        if ((msg == null || msg.isBlank()) && ex.getCause() != null) {
            msg = ex.getCause().getMessage();
        }
        if (msg == null || msg.isBlank()) {
            msg = "未知错误";
        }
        log.error("Unhandled exception: {}", msg, ex);
        return ApiResponse.fail("系统异常: " + msg);
    }
}
