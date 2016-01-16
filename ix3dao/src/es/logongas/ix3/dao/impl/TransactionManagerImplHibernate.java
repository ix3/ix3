/*
 * Copyright 2014 Lorenzo.
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
import es.logongas.ix3.dao.TransactionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Implementación con la sesión de Hibernate
 * @author Lorenzo
 */
public class TransactionManagerImplHibernate implements TransactionManager {


    @Override
    public void begin(DataSession dataSession) {
        
        if (this.isActive(dataSession)==true) {
            throw new RuntimeException("Ya hay una transacción activa");
        }
        
        Session session = ((Session)dataSession.getDataBaseSessionImpl());
        session.beginTransaction();
    }

    @Override
    public void commit(DataSession dataSession) {
        if (isActive(dataSession)==false) {
            throw new RuntimeException("No hay ninguna transacción activa");
        }
        
        Session session = ((Session)dataSession.getDataBaseSessionImpl());
        Transaction transaction=session.getTransaction();
        transaction.commit();
    }

    @Override
    public void rollback(DataSession dataSession) {
        if (isActive(dataSession)==false) {
            throw new RuntimeException("No hay ninguna transacción activa");
        }
        
        Session session = ((Session)dataSession.getDataBaseSessionImpl());
        Transaction transaction=session.getTransaction();
        transaction.rollback();
    }

    @Override
    public boolean isActive(DataSession dataSession) {     
        Session session = ((Session)dataSession.getDataBaseSessionImpl());
        Transaction transaction=session.getTransaction();
        if (transaction==null) {
            return false;
        } else {
            return transaction.isActive();
        } 
    }
    
}
