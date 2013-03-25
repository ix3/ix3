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
import java.lang.reflect.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Creación de DAOs basado en los metadatos que proporciona Hibernate
 *
 * @author Lorenzo González
 */
public class DAOFactoryImplHibernate implements DAOFactory {

    private String domainBasePackageName = null;
    private String daoBasePackageName = null;
    private String daoImplBasePackageName = null;
    @Autowired
    private ApplicationContext context;

    public DAOFactoryImplHibernate() {
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
            fqcn = getFQCNSpecificPackageDAOImpl(entityClass, domainBasePackageName, daoImplBasePackageName);
            daoClass = Class.forName(fqcn);
            genericDAO = (GenericDAO) daoClass.newInstance();
            context.getAutowireCapableBeanFactory().autowireBean(genericDAO);
        } catch (Exception ex) {
            //Si no existe probamos con la siguiente
            try {
                fqcn = getFQCNSamePackageDAOImpl(entityClass, daoImplBasePackageName);
                daoClass = Class.forName(fqcn);
                genericDAO = (GenericDAO) daoClass.newInstance();
                context.getAutowireCapableBeanFactory().autowireBean(genericDAO);
            } catch (Exception ex1) {
                //Si no existe probamos con la siguiente
                //Pero como es generico deberemos ver si existe el interfaz
                GenericDAO realGenericDAO = new GenericDAOImplHibernate(entityClass);
                context.getAutowireCapableBeanFactory().autowireBean(realGenericDAO);
                daoClass=getDAOClass(entityClass);
                if (daoClass==null) {
                    //Si no existe el interfaz no hace falta crear el Proxy pq
                    //sería perder rendimiento.
                   genericDAO=realGenericDAO;
                } else {
                    genericDAO=(GenericDAO)Proxy.newProxyInstance(InvocationHandlerImplDAO.class.getClassLoader(), new Class[] { daoClass }, new InvocationHandlerImplDAO(realGenericDAO));
                }
            }
        }

        return genericDAO;
    }

    /**
     * Busca el interfaz de un DAO. Si éste no existe retorna <code>null</code>
     * @param entityClass
     * @return
     */
    public Class<GenericDAO> getDAOClass(Class entityClass) {
        //Hay 3 formas de encontrar el DAO
        String fqcn;
        Class daoClass;


        try {
            fqcn = getFQCNSpecificPackageDAO(entityClass, domainBasePackageName, daoBasePackageName);
            daoClass = Class.forName(fqcn);
        } catch (Exception ex) {
            //Si no existe probamos con la siguiente
            try {
                fqcn = getFQCNSamePackageDAO(entityClass, daoBasePackageName);
                daoClass = Class.forName(fqcn);
            } catch (Exception ex1) {
                //Si no existe es uqe no hay un interfaz concreto
                //así que usamos GenericDAO como interfaz
                daoClass=null;
            }
        }

        return daoClass;
    }

    /**
     * Establece el nombre del paquete Java donde están los interfaces DAO
     *
     * @param daoBasePackageName Nombre del paquete Java donde están los interfaces DAO
     */
    public void setDaoBasePackageName(String daoBasePackageName) {
        this.daoBasePackageName = daoBasePackageName;
    }

    /**
     * Establece el nombre del paquete Java donde están las implementaciones de los interfaces DAO
     *
     * @param daoBasePackageName Nombre del paquete Java donde están los interfaces DAO
     */
    public void setDaoImplBasePackageName(String daoImplBasePackageName) {
        this.daoImplBasePackageName = daoImplBasePackageName;
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
        if (daoBasePackageName != null) {
            return daoBasePackageName + "." + getDAOClassName(entityClass);
        } else {
            return null;
        }
    }

    protected String getFQCNSpecificPackageDAO(Class entityClass, String domainBasePackageName, String daoBasePackageName) {
        if ((domainBasePackageName != null) && (daoBasePackageName != null)) {
            String packageName = entityClass.getPackage().getName().replace(domainBasePackageName, daoBasePackageName);
            return packageName + "." + getDAOClassName(entityClass);
        } else {
            return null;
        }

    }

    protected String getFQCNSamePackageDAOImpl(Class entityClass, String daoImplBasePackageName) {
        if (daoImplBasePackageName != null) {
            return daoImplBasePackageName + "." + getDAOImplClassName(entityClass);
        } else {
            return null;
        }
    }

    protected String getFQCNSpecificPackageDAOImpl(Class entityClass, String domainBasePackageName, String daoImplBasePackageName) {
        if ((domainBasePackageName != null) && (daoImplBasePackageName != null)) {
            String packageName = entityClass.getPackage().getName().replace(domainBasePackageName, daoImplBasePackageName);
            return packageName + "." + getDAOImplClassName(entityClass);
        } else {
            return null;
        }

    }


    protected String getDAOClassName(Class entityClass) {
        return entityClass.getSimpleName() + "DAO";
    }
    protected String getDAOImplClassName(Class entityClass) {
        return entityClass.getSimpleName() + "DAOImplHibernate";
    }
}
