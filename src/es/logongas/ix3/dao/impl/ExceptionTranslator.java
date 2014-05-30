/*
 * Copyright 2014 Lorenzo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.logongas.ix3.dao.impl;

import es.logongas.ix3.core.annotations.Caption;
import es.logongas.ix3.core.database.impl.ConstraintViolationTranslatorImplMySQL;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.BusinessMessage;
import es.logongas.ix3.core.database.ConstraintViolationTranslator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Clase de utilidad para generar BusinessMessages a partir de excepciones Java.
 * @author Lorenzo
 */
public class ExceptionTranslator {
    
    @Autowired
    ConstraintViolationTranslator constraintViolationTranslator;
    
    public List<BusinessMessage> getBusinessMessages(javax.validation.ConstraintViolationException cve) {
        List<BusinessMessage> businessMessages = new ArrayList<BusinessMessage>();
        
        for (ConstraintViolation constraintViolation : cve.getConstraintViolations()) {
            String propertyName;
            String message;

            propertyName = getPropertyNameFromPath(constraintViolation.getRootBeanClass(), constraintViolation.getPropertyPath());
            message = constraintViolation.getMessage();

            businessMessages.add(new BusinessMessage(propertyName, message));
        }
        
        return businessMessages;
    }

    public List<BusinessMessage> getBusinessMessages(org.hibernate.exception.ConstraintViolationException cve) {
        List<BusinessMessage> businessMessages = new ArrayList<BusinessMessage>();
        BusinessMessage businessMessage;

        String message = cve.getMessage();
        int errorCode = cve.getErrorCode();
        String sqlState = cve.getSQLState();

        es.logongas.ix3.core.database.ConstraintViolation constraintViolation = constraintViolationTranslator.translate(message, errorCode, sqlState);

        if (constraintViolation == null) {
            throw cve;
        } else {
            businessMessage = new BusinessMessage(null,constraintViolation.getMessage());
        }

        businessMessages.add(businessMessage);
        
        return businessMessages;
    }
    
    private String getPropertyNameFromPath(Class clazz, Path path) {
        StringBuilder sb = new StringBuilder();
        if (path != null) {
            Class currentClazz = clazz;
            for (Path.Node node : path) {
                ClassAndCaption clazzAndCaption = getSingleCaption(currentClazz, node.getName());
                if (clazzAndCaption.caption != null) {
                    if (sb.length() != 0) {
                        sb.append(".");
                    }
                    if (node.isInIterable()) {
                        if (node.getIndex() != null) {
                            sb.append(node.getIndex());
                            sb.append("º ");
                            sb.append(clazzAndCaption.caption);
                        } else if (node.getKey() != null) {
                            sb.append(clazzAndCaption.caption);
                            sb.append(" de ");
                            sb.append(node.getKey());
                        } else {
                            sb.append(clazzAndCaption.caption);
                        }
                    } else {
                        sb.append(clazzAndCaption.caption);
                    }
                } else {
                    sb.append("");
                }
                currentClazz = clazzAndCaption.clazz;
            }

            return sb.toString();

        } else {
            return null;
        }

    }

    private ClassAndCaption getSingleCaption(Class clazz, String fieldName) {
        ClassAndCaption clazzAndCaptionField;
        ClassAndCaption clazzAndCaptionMethod;

        if ((fieldName == null) || (fieldName.trim().equals(""))) {
            return new ClassAndCaption(clazz, null);
        }

        clazzAndCaptionField = getFieldCaption(clazz, fieldName);
        if ((clazzAndCaptionField != null) && (clazzAndCaptionField.caption != null)) {
            return clazzAndCaptionField;
        }

        clazzAndCaptionMethod = getMethodCaption(clazz, fieldName);
        if ((clazzAndCaptionMethod != null) && (clazzAndCaptionMethod.caption != null)) {
            return clazzAndCaptionMethod;
        }

        if (clazzAndCaptionField != null) {
            return new ClassAndCaption(clazzAndCaptionField.clazz, fieldName);
        } else if (clazzAndCaptionMethod != null) {
            return new ClassAndCaption(clazzAndCaptionMethod.clazz, fieldName);
        } else {
            return new ClassAndCaption(clazz, fieldName);
        }
    }

    private ClassAndCaption getFieldCaption(Class clazz, String fieldName) {
        Field field = ReflectionUtils.findField(clazz, fieldName);
        if (field == null) {
            return null;
        }

        Caption caption = field.getAnnotation(Caption.class);
        if (caption != null) {
            return new ClassAndCaption(field.getType(), caption.value());
        } else {
            return new ClassAndCaption(field.getType(), null);
        }


    }

    private ClassAndCaption getMethodCaption(Class clazz, String methodName) {
        String suffixMethodName = StringUtils.capitalize(methodName);
        Method method = ReflectionUtils.findMethod(clazz, "get" + suffixMethodName);
        if (method == null) {
            method = ReflectionUtils.findMethod(clazz, "is" + suffixMethodName);
            if (method == null) {
                return null;
            }
        }

        Caption caption = method.getAnnotation(Caption.class);
        if (caption != null) {
            return new ClassAndCaption(method.getReturnType(), caption.value());
        } else {
            return new ClassAndCaption(method.getReturnType(), null);
        }


    }

    private class ClassAndCaption {

        Class clazz;
        String caption;

        public ClassAndCaption(Class clazz, String caption) {
            this.clazz = clazz;
            this.caption = caption;
        }
    }
    
    
}
