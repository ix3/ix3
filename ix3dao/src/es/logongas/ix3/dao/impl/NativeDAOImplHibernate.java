/*
 * Copyright 2015 logongas.
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
import es.logongas.ix3.dao.NativeDAO;
import java.util.List;
import java.util.Map;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * Lanza consultas nativas usando Hibernate
 *
 * @author logongas
 */
public class NativeDAOImplHibernate implements NativeDAO {

    @Override
    public List<Object> createNativeQuery(DataSession dataSession,String query, List<Object> params) {
        Session session = (Session) dataSession.getDataBaseSessionImpl();

        SQLQuery sqlQuery = session.createSQLQuery(query);

        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                sqlQuery.setParameter(i, params.get(i));
            }
        }

        return sqlQuery.list();
    }

    @Override
    public List<Object> createNativeQuery(DataSession dataSession,String query, Map<String, Object> params) {
        Session session = (Session) dataSession.getDataBaseSessionImpl();

        SQLQuery sqlQuery = session.createSQLQuery(query);
        
        if (params != null) {
            for (String paramName : params.keySet()) {
                sqlQuery.setParameter(paramName, params.get(paramName));
            }
        }
        return sqlQuery.list();
    }

}
