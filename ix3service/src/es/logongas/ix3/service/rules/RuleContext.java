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

package es.logongas.ix3.service.rules;

import es.logongas.ix3.security.authentication.Principal;

/**
 *
 * @author logongas
 */
public class RuleContext<T> {
    private T entity;
    private T originalEntity;
    private Principal principal;

    public RuleContext() {
    }

    public RuleContext(T entity, T originalEntity, Principal principal) {
        this.entity = entity;
        this.originalEntity = originalEntity;
        this.principal = principal;
    }

    /**
     * @return the entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * @param entity the entity to set
     */
    public void setEntity(T entity) {
        this.entity = entity;
    }

    /**
     * @return the originalEntity
     */
    public T getOriginalEntity() {
        return originalEntity;
    }

    /**
     * @param originalEntity the originalEntity to set
     */
    public void setOriginalEntity(T originalEntity) {
        this.originalEntity = originalEntity;
    }

    /**
     * @return the principal
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * @param principal the principal to set
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
    
    
    
}
