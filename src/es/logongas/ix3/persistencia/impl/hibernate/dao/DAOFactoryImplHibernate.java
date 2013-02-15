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

import es.logongas.ix3.persistencia.services.dao.DAOFactory;
import es.logongas.ix3.persistencia.services.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Creación de DAOs basado en los metadatos que proporciona Hibernate
 *
 * @author Lorenzo González
 */
public class DAOFactoryImplHibernate implements DAOFactory {

    private String domainBasePackageName;
    private String daoBasePackageName;

    @Autowired
    private ApplicationContext context;

    public DAOFactoryImplHibernate() {
    }

    public DAOFactoryImplHibernate(String domainBasePackageName, String daoBasePackageName) {
        this.domainBasePackageName = domainBasePackageName;
        this.daoBasePackageName = daoBasePackageName;
    }

    /**
     * Obtiene el DAO asociado a una clase de negocio. El DAO debe tener el
     * nombre siguiente DAONombreEntidadImplHibernate. Si no existe una clase
     * específica con ese nombre se retornará GenericDAOImplHibernate. Hay
     * úncamente 2 paquetes donde debe estar la clase
     * DAONombreEntidadImplHibernate En el paquete 'daoBasePackageName' o en el
     * paquete daoBasePackageName y un subpaquete igual a subtituir
     * domainBasePackageName por daoBasePackageName
     *
     * @param EntityClass
     * @return El DAO de la entidad
     */
    @Override
    public GenericDAO getDAO(Class entityClass) {
        //Hay 3 formas de encontrar el DAO
        String fqcn;
        GenericDAO genericDAO;
        Class daoClass;


        try {
            fqcn = getFQCNSpecificPackageDAO(entityClass, domainBasePackageName, daoBasePackageName);
            daoClass = Class.forName(fqcn);
            genericDAO = (GenericDAO) daoClass.newInstance();

        } catch (ClassNotFoundException ex) {
            //Si no existe probamos con la siguiente
            try {
                fqcn = getFQCNSamePackageDAO(entityClass, daoBasePackageName);
                daoClass = Class.forName(fqcn);
                genericDAO = (GenericDAO) daoClass.newInstance();
            } catch (ClassNotFoundException ex1) {
                //Si no existe probamos con la siguiente
                genericDAO = new GenericDAOImplHibernate(entityClass);
            } catch (Exception ex2) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        context.getAutowireCapableBeanFactory().autowireBean(genericDAO);

        return genericDAO;
    }

    /**
     * Establece el nombre del paquete Java donde están los DAO
     *
     * @param daoBasePackageName Nombre del paquete Java donde están los DAO
     */
    public void setDaoBasePackageName(String daoBasePackageName) {
        this.daoBasePackageName = daoBasePackageName;
    }

    /**
     * Establece el nombre del paquete Java donde están las clases de negocio
     *
     * @param domainBasePackageName Nombre del paquete Java donde están las
     * clases de negocio
     */
    public void setDomainBasePackageName(String domainBasePackageName) {
        this.domainBasePackageName = domainBasePackageName;
    }

    protected String getFQCNSamePackageDAO(Class entityClass, String daoBasePackageName) {
        return daoBasePackageName + "." + getDAOClassName(entityClass);
    }

    protected String getFQCNSpecificPackageDAO(Class entityClass, String domainBasePackageName, String daoBasePackageName) {
        String packageName = entityClass.getPackage().getName().replace(domainBasePackageName, daoBasePackageName);

        return packageName + "." + getDAOClassName(entityClass);
    }

    protected String getDAOClassName(Class entityClass) {
        return "DAO" + entityClass.getSimpleName() + "ImplHibernate";
    }
}
