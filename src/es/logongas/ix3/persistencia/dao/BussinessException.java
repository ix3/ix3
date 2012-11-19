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
package es.logongas.ix3.persistencia.dao;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

public class BussinessException extends Exception {

    private List<BussinessMessage> bussinessMessages = new ArrayList<>();

    public BussinessException(List<BussinessMessage> bussinessMessages) {
        this.bussinessMessages.addAll(bussinessMessages);
    }

    public BussinessException(BussinessMessage bussinessMessage) {
        this.bussinessMessages.add(bussinessMessage);
    }

    public BussinessException(Exception ex) {
        bussinessMessages.add(new BussinessMessage(null, ex.toString()));
    }

    public BussinessException(javax.validation.ConstraintViolationException cve) {
        for (ConstraintViolation constraintViolation : cve.getConstraintViolations()) {
            String propertyName;
            String message;

            propertyName = getPropertyNameFromPath(constraintViolation.getPropertyPath());
            message = constraintViolation.getMessage();

            bussinessMessages.add(new BussinessMessage(propertyName, message));
        }
    }

    public BussinessException(org.hibernate.exception.ConstraintViolationException cve) {
        bussinessMessages.add(new BussinessMessage(null, cve.getLocalizedMessage()));
    }

    public List<BussinessMessage> getBussinessMessages() {
        return bussinessMessages;
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
}
