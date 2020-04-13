package com.apress.todo.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class TodoValidationErrorBuilder {
    public static TodoValidationError fromBindingError(Errors errors) {
        TodoValidationError error = new TodoValidationError("Validation Failed. " + errors.getErrorCount() + " error(s)");
        for(ObjectError objectError:errors.getAllErrors()){
            error.addValidationError(objectError.getDefaultMessage());
        }
        return error;
    }
}