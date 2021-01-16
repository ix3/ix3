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

import es.logongas.ix3.core.Page;
import java.util.List;

/**
 * Implementacion de la paginación
 * @author Lorenzo
 * @param <T>
 */
public class PageImpl<T> implements Page<T> {

    private final List<T> content;
    private final int pageSize;
    private final int pageNumber;
    private final int totalPages;
    private final long totalRows;

    public PageImpl(List<T> content, int pageSize, int pageNumber, int totalPages, long totalRows) {
        this.content = content;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
        this.totalRows = totalRows;
    }

    /**
     * @return the content
     */
    @Override
    public List<T> getContent() {
        return content;
    }

    /**
     * @return the pageSize
     */
    @Override
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @return the pageNumber
     */
    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return the totalPages
     */
    @Override
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public long getTotalRows() {
        return totalRows;
    }
    
}
