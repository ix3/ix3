/*
 * Copyright 2012 Lorenzo González.
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
package es.logongas.ix3.persistence.services.dao;

import es.logongas.ix3.persistence.impl.database.mysql.ConstraintViolationTranslatorImplMySQL;
import es.logongas.ix3.persistence.services.dao.database.ConstraintViolationTranslator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

public class BusinessException extends Exception {

    private List<BusinessMessage> businessMessages = new ArrayList<BusinessMessage>();

    public BusinessException(List<BusinessMessage> businessMessages) {
        this.businessMessages.addAll(businessMessages);
    }

    public BusinessException(BusinessMessage businessMessage) {
        this.businessMessages.add(businessMessage);
    }

    public BusinessException(Exception ex) {
        businessMessages.add(new BusinessMessage(null, ex.toString()));
    }

    public BusinessException(javax.validation.ConstraintViolationException cve) {
        for (ConstraintViolation constraintViolation : cve.getConstraintViolations()) {
            String propertyName;
            String message;

            propertyName = getPropertyNameFromPath(constraintViolation.getPropertyPath());
            message = constraintViolation.getMessage();

            businessMessages.add(new BusinessMessage(propertyName, message));
        }
    }

    public BusinessException(org.hibernate.exception.ConstraintViolationException cve) {
        BusinessMessage businessMessage;

        String message = cve.getMessage();
        int errorCode = cve.getErrorCode();
        String sqlState = cve.getSQLState();

        ConstraintViolationTranslator constraintViolationTranslator = new ConstraintViolationTranslatorImplMySQL();
        es.logongas.ix3.persistence.services.dao.database.ConstraintViolation constraintViolation = constraintViolationTranslator.translate(message, errorCode, sqlState);

        if (constraintViolation == null) {
            throw cve;
        } else {
            businessMessage = new BusinessMessage(constraintViolation.getPropertyName(), constraintViolation.getMessage());
        }

        businessMessages.add(businessMessage);
    }

    public List<BusinessMessage> getBusinessMessages() {
        return businessMessages;
    }

    private String getPropertyNameFromPath(Path path) {
        StringBuilder sb = new StringBuilder();
        if (path != null) {
            for (Path.Node node : path) {
                String propertyName = node.getName();
                if (propertyName != null) {
                    if (sb.length() != 0) {
                        sb.append(".");
                    }
                    if (node.isInIterable()) {
                        if (node.getIndex() != null) {
                            sb.append(node.getIndex());
                            sb.append("º ");
                            sb.append(propertyName);
                        } else if (node.getKey() != null) {
                            sb.append(propertyName);
                            sb.append(" de ");
                            sb.append(node.getKey());
                        } else {
                            sb.append(propertyName);
                        }
                    } else {
                        sb.append(propertyName);
                    }
                } else {
                    sb.append("");
                }
            }

            return sb.toString();
        } else {
            return null;
        }

    }
    
    private String getDetailMessage() {
        StringBuilder sb=new StringBuilder();
        
        if (businessMessages!=null) {
            for(BusinessMessage businessMessage:businessMessages) {
                if (businessMessage!=null) {
                    try {
                        sb.append(businessMessage.toString());
                    } catch (Exception ex) {
                       sb.append(ex.getMessage());
                    }
                    sb.append("|");
                }
            }
        }
        
        return sb.toString();
    }

    @Override
    public String getMessage() {
        return getDetailMessage();
    }    
    
    @Override
    public String toString() {
        return getDetailMessage();
    }
    
}
