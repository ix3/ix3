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

import es.logongas.ix3.core.annotations.Label;
import es.logongas.ix3.core.BusinessMessage;
import es.logongas.ix3.core.database.ConstraintViolationTranslator;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.metadata.MetaType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Clase de utilidad para generar BusinessMessages a partir de excepciones Java.
 *
 * @author Lorenzo
 */
public class ExceptionTranslator {

    @Autowired
    ConstraintViolationTranslator constraintViolationTranslator;
    
    @Autowired
    MetaDataFactory metaDataFactory;

    public List<BusinessMessage> getBusinessMessages(javax.validation.ConstraintViolationException cve) {
        List<BusinessMessage> businessMessages = new ArrayList<>();

        for (ConstraintViolation constraintViolation : cve.getConstraintViolations()) {
            String propertyName;
            String message;

            propertyName = getPropertyNameFromPath(constraintViolation.getRootBeanClass(), constraintViolation.getPropertyPath());
            message = constraintViolation.getMessage();

            businessMessages.add(new BusinessMessage(propertyName, message));
        }

        return businessMessages;
    }

    public List<BusinessMessage> getBusinessMessages(org.hibernate.exception.ConstraintViolationException cve,Class entityType) {
        List<BusinessMessage> businessMessages = new ArrayList<>();
        BusinessMessage businessMessage;

        String message;
        int errorCode;
        String sqlState;

        if (cve.getSQLException() != null) {
            message = cve.getSQLException().getLocalizedMessage();
            errorCode = cve.getSQLException().getErrorCode();
            sqlState = cve.getSQLException().getSQLState();
        } else {
            message = cve.getLocalizedMessage();
            errorCode = cve.getErrorCode();
            sqlState = cve.getSQLState();
        }

        es.logongas.ix3.core.database.ConstraintViolation constraintViolation = constraintViolationTranslator.translate(message, errorCode, sqlState);

        if (constraintViolation == null) {
            throw cve;
        } else {
            
            String propertyName;
            if (entityType!=null) {
                MetaData metaData=metaDataFactory.getMetaData(entityType);
                propertyName=getPropertyNameFromDataBasePropertyName(metaData,constraintViolation.getPropertyName());
            } else {
                propertyName=constraintViolation.getPropertyName();
            }
            businessMessage = new BusinessMessage(propertyName,constraintViolation.getMessage());

        }

        businessMessages.add(businessMessage);

        return businessMessages;
    }
    public List<BusinessMessage> getBusinessMessages(org.hibernate.exception.DataException de,Class entityType) {
        List<BusinessMessage> businessMessages = new ArrayList<>();
        BusinessMessage businessMessage;
        String message;
        int errorCode;
        String sqlState;

        if (de.getSQLException() != null) {
            message = de.getSQLException().getLocalizedMessage();
            errorCode = de.getSQLException().getErrorCode();
            sqlState = de.getSQLException().getSQLState();
        } else {
            message = de.getLocalizedMessage();
            errorCode = de.getErrorCode();
            sqlState = de.getSQLState();
        }

        es.logongas.ix3.core.database.ConstraintViolation constraintViolation = constraintViolationTranslator.translate(message, errorCode, sqlState);

        if (constraintViolation == null) {
            throw de;
        } else {
            String propertyName;
            
            if (entityType!=null) {
                MetaData metaData=metaDataFactory.getMetaData(entityType);
                propertyName=getPropertyNameFromDataBasePropertyName(metaData,constraintViolation.getPropertyName());
            } else {
                propertyName=constraintViolation.getPropertyName();
            }
            
            businessMessage = new BusinessMessage(propertyName,constraintViolation.getMessage());
        }

        businessMessages.add(businessMessage);

        return businessMessages;
    }
    private String getPropertyNameFromPath(Class clazz, Path path) {
        StringBuilder sb = new StringBuilder();
        if (path != null) {
            Class currentClazz = clazz;
            for (Path.Node node : path) {
                ClassAndLabel clazzAndCaption = getSingleCaption(currentClazz, node.getName());
                if (clazzAndCaption.label != null) {
                    if (sb.length() != 0) {
                        sb.append(" ");
                    }
                    if (node.isInIterable()) {
                        if (node.getIndex() != null) {
                            sb.append(node.getIndex());
                            sb.append("º ");
                            sb.append(clazzAndCaption.label);
                        } else if (node.getKey() != null) {
                            sb.append(clazzAndCaption.label);
                            sb.append(" de ");
                            sb.append(node.getKey());
                        } else {
                            sb.append(clazzAndCaption.label);
                        }
                    } else {
                        sb.append(clazzAndCaption.label);
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

    private String getPropertyNameFromDataBasePropertyName(MetaData metaData, String dataBasePropertyName) {

        Set<String> propertyNames=metaData.getPropertiesMetaData().keySet();
        for (String propertyName:propertyNames) {
            MetaData metaDataProperty=metaData.getPropertyMetaData(propertyName);
            
            if (propertyName.equalsIgnoreCase(dataBasePropertyName)) {
                return metaDataProperty.getLabel();
            } else if ((metaDataProperty.isCollection()==false) && (metaDataProperty.getMetaType()==MetaType.Component)) {
                String realPropertyName=getPropertyNameFromDataBasePropertyName(metaDataProperty,dataBasePropertyName);
                
                if (realPropertyName!=null) {
                    return realPropertyName;
                }
            }
        }
        
        return null;
        
    }
    
    
    
    private ClassAndLabel getSingleCaption(Class clazz, String fieldName) {
        ClassAndLabel clazzAndLabelField;
        ClassAndLabel clazzAndLabelMethod;

        if ((fieldName == null) || (fieldName.trim().equals(""))) {
            return new ClassAndLabel(clazz, null);
        }

        clazzAndLabelField = getFieldLabel(clazz, fieldName);
        if ((clazzAndLabelField != null) && (clazzAndLabelField.label != null)) {
            return clazzAndLabelField;
        }

        clazzAndLabelMethod = getMethodLabel(clazz, fieldName);
        if ((clazzAndLabelMethod != null) && (clazzAndLabelMethod.label != null)) {
            return clazzAndLabelMethod;
        }

        if (clazzAndLabelField != null) {
            return new ClassAndLabel(clazzAndLabelField.clazz, fieldName);
        } else if (clazzAndLabelMethod != null) {
            return new ClassAndLabel(clazzAndLabelMethod.clazz, fieldName);
        } else {
            return new ClassAndLabel(clazz, fieldName);
        }
    }

    private ClassAndLabel getFieldLabel(Class clazz, String fieldName) {
        Field field = ReflectionUtils.findField(clazz, fieldName);
        if (field == null) {
            return null;
        }

        Label label = field.getAnnotation(Label.class);
        if (label != null) {
            return new ClassAndLabel(field.getType(), label.value());
        } else {
            return new ClassAndLabel(field.getType(), null);
        }

    }

    private ClassAndLabel getMethodLabel(Class clazz, String methodName) {
        String suffixMethodName = StringUtils.capitalize(methodName);
        Method method = ReflectionUtils.findMethod(clazz, "get" + suffixMethodName);
        if (method == null) {
            method = ReflectionUtils.findMethod(clazz, "is" + suffixMethodName);
            if (method == null) {
                return null;
            }
        }

        Label label = method.getAnnotation(Label.class);
        if (label != null) {
            return new ClassAndLabel(method.getReturnType(), label.value());
        } else {
            return new ClassAndLabel(method.getReturnType(), null);
        }

    }

    private class ClassAndLabel {

        Class clazz;
        String label;

        public ClassAndLabel(Class clazz, String label) {
            this.clazz = clazz;
            this.label = label;
        }
    }

}
