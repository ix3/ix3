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
package es.logongas.ix3.core.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static SessionFactory sessionFactory2;

    public static synchronized void buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure();

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        ServiceRegistry serviceRegistry2 = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory2 = configuration.buildSessionFactory(serviceRegistry2);

        //new org.hibernate.tool.hbm2ddl.SchemaExport(configuration).setOutputFile("script.sql").setDelimiter(";").create(true, false);
    }

    public static void closeSessionFactory() {
        if ((sessionFactory!=null) && (sessionFactory.isClosed()==false)) {
            sessionFactory.close();
        }
        if ((sessionFactory2!=null) && (sessionFactory2.isClosed()==false)) {
            sessionFactory2.close();
        }
    }


    public static SessionFactory getSessionFactory() {
        if (sessionFactory==null)  {
            buildSessionFactory();
        }
        if (sessionFactory.isClosed()==true) {
            throw new RuntimeException("El objeto sessionFactory está cerrado");
        }
        return sessionFactory;
    }
    public static SessionFactory getSessionFactory2() {
        if (sessionFactory2==null)  {
            buildSessionFactory();
        }
        if (sessionFactory2.isClosed()==true) {
            throw new RuntimeException("El objeto sessionFactory2 está cerrado");
        }
        return sessionFactory2;
    }
    

}
