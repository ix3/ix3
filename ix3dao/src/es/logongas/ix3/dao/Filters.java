/*
 * Copyright 2016 logongas.
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

import java.util.ArrayList;

/**
 *
 * @author logongas
 */
public class Filters extends ArrayList<Filter> {
    

    /**
     * Busca un filtro con nombre 'propertyName' pero si hay mas de uno con ese nombre, retorna null. Además filtra por el operador 'withFilterOperator'
     * @param propertyName
     * @param withFilterOperator
     * @return 
     */
    public Filter getUniquePropertyNameFilter(String propertyName,FilterOperator withFilterOperator) {
        Filter searchFilter=null;
        
        for(Filter filter:this) {
            if (filter.getPropertyName().equals(propertyName)) {
                if (searchFilter!=null) {
                    return null;
                }
                
                if (filter.getFilterOperator()==withFilterOperator) {
                    searchFilter = filter;
                }
            }
        }
        
        return searchFilter;
    }
    
}
