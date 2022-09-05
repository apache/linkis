package org.apache.linkis.configuration.util;

import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.governance.common.entity.job.QueryException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class CommonUtils {

    public static void validateObject(Object object) throws ConfigurationException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
            throw new ConfigurationException(constraintViolation.getPropertyPath() + ",不能为空");
        }
    }
}
