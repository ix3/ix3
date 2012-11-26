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
package es.logongas.ix3.persistencia.impl.hibernate.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static void buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure();
        if ((configuration.getProperty("current_session_context_class")!=null) && (configuration.getProperty("current_session_context_class").equals("thread")==false)) {
            throw new RuntimeException("Hibernate debe estar configurado en el fichero 'hibernate.cfg.xml' con la propiedad current_session_context_class=thread");
        }
        
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry); 
    }

    public static void closeSessionFactory() {
        sessionFactory.close();
    }

    public static void openSessionAndAttachToThread() {
        Session session = sessionFactory.openSession();
        ThreadLocalSessionContext.bind(session);
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void closeSessionAndDeattachFromThread() {
        Session session = ThreadLocalSessionContext.unbind(sessionFactory);
        if (session!=null) {
            session.close();
        }
    }

}
