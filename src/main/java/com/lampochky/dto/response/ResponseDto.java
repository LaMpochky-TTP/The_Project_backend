package com.lampochky.dto.response;

import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class ResponseDto {
    protected boolean success;
    protected List<Error> errors;

    public ResponseDto(List<Error> errors) {
        if(errors == null){
            errors = Collections.emptyList();
        }
        this.errors = errors;
        this.success = errors.isEmpty();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
