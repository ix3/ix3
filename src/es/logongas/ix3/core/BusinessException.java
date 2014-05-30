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
package es.logongas.ix3.core;

import es.logongas.ix3.core.annotations.Caption;
import es.logongas.ix3.core.database.impl.ConstraintViolationTranslatorImplMySQL;
import es.logongas.ix3.core.database.ConstraintViolationTranslator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class BusinessException extends Exception {

    private final List<BusinessMessage> businessMessages = new ArrayList<BusinessMessage>();

    public BusinessException(List<BusinessMessage> businessMessages) {
        this.businessMessages.addAll(businessMessages);
    }

    public BusinessException(BusinessMessage businessMessage) {
        this.businessMessages.add(businessMessage);
    }




    public List<BusinessMessage> getBusinessMessages() {
        return businessMessages;
    }

 
    private String getDetailMessage() {
        StringBuilder sb = new StringBuilder();

        if (businessMessages != null) {
            for (BusinessMessage businessMessage : businessMessages) {
                if (businessMessage != null) {
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
