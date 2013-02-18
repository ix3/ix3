/*
 * Copyright 2013 Lorenzo González.
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

import es.logongas.ix3.persistencia.services.dao.GenericDAO;
import es.logongas.ix3.test.datos.prueba.EntidadDAOImplHibernate;
import es.logongas.ix3.test.negocio.prueba.Entidad;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Lorenzo González
 */
public class DAOFactoryImplHibernateTest {
    
    public DAOFactoryImplHibernateTest() {
    }

    @Test
    public void testGetDAO() {
        System.out.println("getDAO");
        
        ApplicationContext context =new ClassPathXmlApplicationContext("classpath:ix3ApplicationContext.xml");
        String domainBasePackageName="es.logongas.ix3.test.negocio";
        String daoBasePackageName="es.logongas.ix3.test.datos";
        
        DAOFactoryImplHibernate instance =context.getBean(DAOFactoryImplHibernate.class);
        instance.setDomainBasePackageName(domainBasePackageName);
        instance.setDaoBasePackageName(daoBasePackageName);
        
        Class entityClass = Entidad.class;
        Class expResult = EntidadDAOImplHibernate.class;
        GenericDAO result = instance.getDAO(entityClass);
        assertEquals(expResult, result.getClass());
        
        ((GenericDAOImplHibernate)result).getEntityMetaData();
        
    }



    @Test
    public void testGetFQCNSamePackageDAO() {
        System.out.println("getFQCNSamePackageDAO");
        
        ApplicationContext context =new ClassPathXmlApplicationContext("classpath:ix3ApplicationContext.xml");
        String domainBasePackageName="es.logongas.ix3.test.negocio";
        String daoBasePackageName="es.logongas.ix3.test.datos";
        
        DAOFactoryImplHibernate instance =context.getBean(DAOFactoryImplHibernate.class);
        instance.setDomainBasePackageName(domainBasePackageName);
        instance.setDaoBasePackageName(daoBasePackageName);
        
        Class entityClass = Entidad.class;
        String expResult = daoBasePackageName + "." + "DAOEntidadImplHibernate";
        String result = instance.getFQCNSamePackageDAO(entityClass, daoBasePackageName);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFQCNSpecificPackageDAO() {
        System.out.println("getFQCNSpecificPackageDAO");
        ApplicationContext context =new ClassPathXmlApplicationContext("classpath:ix3ApplicationContext.xml");
        String domainBasePackageName="es.logongas.ix3.test.negocio";
        String daoBasePackageName="es.logongas.ix3.test.datos";
        
        DAOFactoryImplHibernate instance =context.getBean(DAOFactoryImplHibernate.class);
        instance.setDomainBasePackageName(domainBasePackageName);
        instance.setDaoBasePackageName(daoBasePackageName);
        
        Class entityClass = Entidad.class;
        String expResult = daoBasePackageName + ".prueba." + "DAOEntidadImplHibernate";
        String result = instance.getFQCNSpecificPackageDAO(entityClass, domainBasePackageName, daoBasePackageName);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDAOClassName() {
        System.out.println("getDAOClassName");
        
        ApplicationContext context =new ClassPathXmlApplicationContext("classpath:ix3ApplicationContext.xml");
        String domainBasePackageName="es.logongas.ix3.test.negocio";
        String daoBasePackageName="es.logongas.ix3.test.datos";
        
        DAOFactoryImplHibernate instance =context.getBean(DAOFactoryImplHibernate.class);
        instance.setDomainBasePackageName(domainBasePackageName);
        instance.setDaoBasePackageName(daoBasePackageName);        
        
        Class entityClass = Entidad.class;
        String expResult = "DAOEntidadImplHibernate";
        String result = instance.getDAOClassName(entityClass);
        assertEquals(expResult, result);
    }
}
