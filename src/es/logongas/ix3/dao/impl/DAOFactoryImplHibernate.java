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
package es.logongas.ix3.dao.impl;

import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.util.FactoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Creación de DAOs basado en los metadatos que proporciona Hibernate
 *
 * @author Lorenzo González
 */
public class DAOFactoryImplHibernate implements DAOFactory {

    private String domainBasePackageName;
    private String interfaceBasePackageName;
    private String implBasePackageName;
    private final String interfaceSufix = "DAO";
    private final String implSufix = "DAOImplHibernate";
    private final Class<? extends GenericDAO> defaultImplClass = GenericDAOImplHibernate.class;
            
    @Autowired
    private ApplicationContext context;

    /**
     * Obtiene el DAO asociado a una clase de negocio. La implementación del DAO
     * debe tener el nombre siguiente DAONombreEntidadImplHibernate. Si no
     * existe una clase específica con ese nombre se retornará
     * GenericDAOImplHibernate. Hay úncamente 2 paquetes donde debe estar la
     * clase DAONombreEntidadImplHibernate En el paquete
     * 'interfaceBasePackageName' o en el paquete interfaceBasePackageName y un
     * subpaquete igual a subtituir domainBasePackageName por
     * interfaceBasePackageName. Lo mismo pasa con el propio interfaz que estará
     * en "implBasePackageName
     *
     * @param EntityClass
     * @return El DAO de la entidad
     */
    @Override
    public GenericDAO getDAO(Class entityClass) {
        FactoryHelper<GenericDAO> factoryHelper = new FactoryHelper<GenericDAO>(domainBasePackageName, interfaceBasePackageName, implBasePackageName, interfaceSufix, implSufix, defaultImplClass, context);

        return factoryHelper.getImpl(entityClass);
    }
    
    /**
     * @param domainBasePackageName the domainBasePackageName to set
     */
    public void setDomainBasePackageName(String domainBasePackageName) {
        this.domainBasePackageName = domainBasePackageName;
    }

    /**
     * @param interfaceBasePackageName the interfaceBasePackageName to set
     */
    public void setInterfaceBasePackageName(String interfaceBasePackageName) {
        this.interfaceBasePackageName = interfaceBasePackageName;
    }

    /**
     * @param implBasePackageName the implBasePackageName to set
     */
    public void setImplBasePackageName(String implBasePackageName) {
        this.implBasePackageName = implBasePackageName;
    }
}
