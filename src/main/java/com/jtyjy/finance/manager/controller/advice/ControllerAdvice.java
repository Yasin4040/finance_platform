package com.jtyjy.finance.manager.controller.advice;

import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.result.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    /**
     * 必传参数缺省异常
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<String> exceptionHandler(MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, e.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValidHandler(MethodArgumentNotValidException exception) {
        return ResponseEntity.apply(StatusCodeEnmus.OTHER, exception.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(";")), null);
    }

    /**
     * 未知异常统一处理
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.apply(StatusCodeEnmus.OTHER, e.getMessage(), e.getMessage());
    }

}
