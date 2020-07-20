/*
 * ix3 Copyright 2020 Lorenzo González.
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
