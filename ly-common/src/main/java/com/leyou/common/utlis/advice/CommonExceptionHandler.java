package com.leyou.common.utlis.advice;

import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 拦截返回的指定异常回显给用户浏览器
 */
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(LyException.class) //指定要拦截的异常
    public ResponseEntity<String> handlerExecption(LyException e){
        ExceptionEnum em = e.getExceptionEnum();
        //向用户浏览器返回BAD_REQUEST状态码（400）和错误信息
        return ResponseEntity.status(em.getCode()).body(em.getMsg());
    }
}
