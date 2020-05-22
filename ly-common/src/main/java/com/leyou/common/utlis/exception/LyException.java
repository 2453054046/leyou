package com.leyou.common.utlis.exception;

import com.leyou.common.utlis.enums.ExceptionEnum;

public class LyException extends RuntimeException {

    private ExceptionEnum exceptionEnum;

    LyException(){
    }

    public LyException(ExceptionEnum exceptionEnum){
        this.exceptionEnum = exceptionEnum;
    }

    public ExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }

    public void setExceptionEnum(ExceptionEnum exceptionEnum) {
        this.exceptionEnum = exceptionEnum;
    }
}
