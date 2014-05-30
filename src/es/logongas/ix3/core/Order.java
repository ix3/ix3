/*
 * Copyright 2013 Lorenzo González.
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

/**
 * Campo para ordenar
 * @author Lorenzo González
 */
public class Order {
    private String fieldName;
    private OrderDirection orderDirection;

    public Order(String fieldName, OrderDirection orderDirection) {
        if (fieldName==null) {
            throw new IllegalArgumentException("El campo fieldName no puede ser null");
        }
        if (orderDirection==null) {
            throw new IllegalArgumentException("El campo orderDirection no puede ser null");
        }

        this.fieldName = fieldName;
        this.orderDirection = orderDirection;
    }



    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return the orderDirection
     */
    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

}
