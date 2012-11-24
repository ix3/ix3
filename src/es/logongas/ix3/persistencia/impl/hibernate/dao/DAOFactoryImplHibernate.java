/*
 * Copyright 2012 Lorenzo González.
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
package es.logongas.ix3.persistencia.impl.hibernate.dao;

import es.logongas.ix3.persistencia.dao.DAOFactory;
import es.logongas.ix3.persistencia.dao.GenericDAO;

/**
 * Creación de DAOs basado en los metadatos que proporciona Hibernate 
 * @author Lorenzo González
 */
public class DAOFactoryImplHibernate implements DAOFactory {

    String packageName;
    
    @Override
    public GenericDAO getDAO(Class EntityClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GenericDAO getDAO(String EntityName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Establece el nombre del paquete Java donde están los DAO
     * @param packageName Nombre del paquete Java donde están los DAO
     */
    public void setPackageName(String packageName) {
        this.packageName=packageName;
    }
    
}
