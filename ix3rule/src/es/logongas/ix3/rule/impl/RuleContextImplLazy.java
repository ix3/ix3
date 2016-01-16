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

package es.logongas.ix3.rule.impl;

import es.logongas.ix3.rule.*;
import es.logongas.ix3.core.Principal;

/**
 * No carla el valor de OriginalEntity si no hace falta. 
 * De esta forma mejoramos el rendimiento al no tener que hacer peticiones innecesarias a la base de datos.
 * @author logongas
 * @param <T>
 */
public class RuleContextImplLazy<T> implements RuleContext<T> {
    private final T entity;
    private final Principal principal;
    private final OriginalEntityCallback<T> originalEntityCallback;

    private boolean isLoadedOriginalEntity=false;
    private T originalEntity=null;
    
    public RuleContextImplLazy(T entity, Principal principal,OriginalEntityCallback<T> originalEntityCallback) {
        this.entity = entity;
        this.principal = principal;
        this.originalEntityCallback=originalEntityCallback;
    }
    
    
    /**
     * @return the entity
     */
    @Override
    public T getEntity() {
        return entity;
    }

    /**
     * @return the principal
     */
    @Override
    public Principal getPrincipal() {
        return principal;
    } 
    

    /**
     * @return the originalEntity
     */
    @Override
    public T getOriginalEntity() {
        
        if (isLoadedOriginalEntity==false) {
            originalEntity=originalEntityCallback.get(entity);
            isLoadedOriginalEntity=true;
        }
        
        return originalEntity;
    }
    
    
    public interface OriginalEntityCallback<T> {
        
        T get(T entity);
        
    }
    
}
