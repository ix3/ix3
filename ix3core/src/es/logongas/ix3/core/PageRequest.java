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

package es.logongas.ix3.core;

/**
 * Peticion de una página
 * @author logongas
 */
public class PageRequest {
    private final int pageNumber;
    private final int pageSize;

    public PageRequest(int pageNumber, int pageSize) {
        
        if (pageNumber < 0) {
            throw new RuntimeException("El agumento pageNumber no pude ser negativo");
        }
        if (pageSize < 1) {
            throw new RuntimeException("El agumento pageNumber debe ser mayor que 0");
        }        
        
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    
    
    
    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }


    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

}
