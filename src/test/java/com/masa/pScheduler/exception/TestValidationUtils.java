package com.masa.pScheduler.exception;

import org.mockito.Mockito;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

public class TestValidationUtils {

    public static MethodArgumentNotValidException mockValidationException(String field, String message) {
        try {
            Method method = DummyController.class.getDeclaredMethod("dummyMethod", String.class);
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
            bindingResult.addError(new FieldError("object", field, message));
            return new MethodArgumentNotValidException(null, bindingResult);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DummyController {
        public void dummyMethod(String arg) {}
    }
}
