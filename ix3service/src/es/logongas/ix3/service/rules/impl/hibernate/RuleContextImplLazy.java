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

package es.logongas.ix3.service.rules.impl.hibernate;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.service.rules.*;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.util.UnckeckException;
import java.io.Serializable;

/**
 * No carla el valor de OriginalEntity si no hace falta. 
 * De esta forma mejoramos el rendimiento al no tener que hacer peticiones innecesarias a la base de datos.
 * @author logongas
 * @param <T>
 */
public class RuleContextImplLazy<T> implements RuleContext<T> {
    private final T entity;
    private final Principal principal;
    private final DAOFactory daoFactory;
    private final MetaDataFactory metaDataFactory;

    private boolean isLoadedOriginalEntity=false;
    private T originalEntity=null;
    
    public RuleContextImplLazy(T entity, Principal principal, DAOFactory daoFactory, MetaDataFactory metaDataFactory) {
        this.entity = entity;
        this.principal = principal;
        this.daoFactory = daoFactory;
        this.metaDataFactory = metaDataFactory;
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
            originalEntity=getFromDAOOriginalEntity(entity);
            isLoadedOriginalEntity=true;
        }
        
        return originalEntity;
    }

    
    private <T> T getFromDAOOriginalEntity(T entity) {

        if (entity==null) {
            return null;
        }
        
        MetaData metaData = metaDataFactory.getMetaData(entity.getClass());
        GenericDAO genericDAO = daoFactory.getDAO(entity.getClass());

        Serializable primaryKey = (Serializable) ReflectionUtil.getValueFromBean(entity, metaData.getPrimaryKeyPropertyName());

        try {
            T originalEntity = (T)genericDAO.readOriginal(primaryKey);
            
            return originalEntity;
        } catch (BusinessException ex) {
            UnckeckException.throwCkeckedExceptionAsUnckeckedException(ex);
            
            return null;
        }
    }     
    
}
