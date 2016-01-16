/*
 * Copyright 2016 logongas.
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

import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.DataSessionFactory;
import es.logongas.ix3.dao.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class DataSessionFactoryImplHibernate implements DataSessionFactory {
    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected SessionFactory sessionFactory2;

    @Autowired
    protected TransactionManager transactionManager;    
    
    @Override
    public DataSession getDataSession() {
        Map<String,Object> sessions=new HashMap<String,Object>();
        
        sessions.put("MAIN", sessionFactory.openSession());
        sessions.put("SECONDARY", sessionFactory2.openSession());
        
        return new DataSessionImpl(sessions,this);
    }

    @Override
    public void close(DataSession dataSession) {
        
        boolean isActive=transactionManager.isActive(dataSession);
        
        ((Session)dataSession.getDataBaseSessionImpl()).close();
        ((Session)dataSession.getDataBaseSessionAlternativeImpl()).close();
        
        if (isActive) {
            throw new RuntimeException("Al cerrar la dataSession hay una trasacción activa");
        }
        
    }
    
    
    
    

}
