/*
 * Copyright 2015 Lorenzo Gonzalez.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.logongas.ix3.service.rules.impl;

import es.logongas.ix3.service.rules.*;
import es.logongas.ix3.security.authentication.Principal;

/**
 *
 * @author logongas
 * @param <T>
 */
public class RuleContextImpl<T> implements RuleContext<T> {
    private final T entity;
    private final T originalEntity;
    private final Principal principal;

    public RuleContextImpl(T entity, T originalEntity, Principal principal) {
        this.entity = entity;
        this.originalEntity = originalEntity;
        this.principal = principal;
    }

    /**
     * @return the entity
     */
    @Override
    public T getEntity() {
        return entity;
    }


    /**
     * @return the originalEntity
     */
    @Override
    public T getOriginalEntity() {
        return originalEntity;
    }

    /**
     * @return the principal
     */
    @Override
    public Principal getPrincipal() {
        return principal;
    }    
    
}
