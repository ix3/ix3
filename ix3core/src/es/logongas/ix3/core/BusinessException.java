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

import java.util.ArrayList;
import java.util.List;

public class BusinessException extends Exception {

    private final List<BusinessMessage> businessMessages = new ArrayList<BusinessMessage>();

    public BusinessException(List<BusinessMessage> businessMessages) {
        this.businessMessages.addAll(businessMessages);
    }

    public BusinessException(BusinessMessage businessMessage) {
        this.businessMessages.add(businessMessage);
    }


    public BusinessException(String message) {
        this.businessMessages.add(new BusinessMessage(message));
    }

    public BusinessException(String propertyName, String message) {
        this.businessMessages.add(new BusinessMessage(propertyName, message));
    } 
    public BusinessException(String message, Class<? extends BusinessMessageUID> businessMessageUID) {
        this.businessMessages.add(new BusinessMessage(message, businessMessageUID));
    }

    public BusinessException(String propertyName, String message, Class<? extends BusinessMessageUID> businessMessageUID) {
        this.businessMessages.add(new BusinessMessage(propertyName, message, businessMessageUID));
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
