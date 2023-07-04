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

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.TransactionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementación con la sesión de Hibernate
 *
 * @author Lorenzo
 */
public class TransactionManagerImplHibernate implements TransactionManager {

    @Autowired
    protected ExceptionTranslator exceptionTranslator;

    @Override
    public void begin(DataSession dataSession) throws BusinessException {
        try {
            if (this.isActive(dataSession) == true) {
                throw new RuntimeException("Ya hay una transacción activa");
            }

            Session session = ((Session) dataSession.getDataBaseSessionImpl());
            session.beginTransaction();
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve, null));
        } catch (org.hibernate.exception.DataException de) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(de, null));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void commit(DataSession dataSession) throws BusinessException {
        try {
            if (isActive(dataSession) == false) {
                throw new RuntimeException("No hay ninguna transacción activa");
            }

            Session session = ((Session) dataSession.getDataBaseSessionImpl());
            Transaction transaction = session.getTransaction();
            transaction.commit();
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve, null));
        } catch (org.hibernate.exception.DataException de) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(de, null));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void rollback(DataSession dataSession) throws BusinessException {
        try {
            if (isActive(dataSession) == false) {
                throw new RuntimeException("No hay ninguna transacción activa");
            }

            Session session = ((Session) dataSession.getDataBaseSessionImpl());
            Transaction transaction = session.getTransaction();
            transaction.rollback();
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve, null));
        } catch (org.hibernate.exception.DataException de) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(de, null));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isActive(DataSession dataSession) {

        Session session = ((Session) dataSession.getDataBaseSessionImpl());
        Transaction transaction = session.getTransaction();
        if (transaction == null) {
            return false;
        } else {
            return transaction.isActive();
        }

    }

}
