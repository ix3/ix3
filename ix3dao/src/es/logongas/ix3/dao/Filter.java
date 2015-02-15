/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.dao;

/**
 *
 * @author logongas
 */
public class Filter {

    private String propertyName;
    private Object value;
    private FilterOperator filterOperator;

    public Filter(String propertyName,Object value, FilterOperator filterOperator) {
        this.propertyName = propertyName;
        this.value=value;
        this.filterOperator = filterOperator;
    }

    public Filter(String propertyName,Object value) {
        this.propertyName = propertyName;
        this.value=value;
        this.filterOperator = FilterOperator.eq;  
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return the filterOperator
     */
    public FilterOperator getFilterOperator() {
        return filterOperator;
    }

    /**
     * @param filterOperator the filterOperator to set
     */
    public void setFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }
    
    

}
