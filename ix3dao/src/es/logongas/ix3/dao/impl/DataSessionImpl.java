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
import java.util.Map;

/**
 * 
 * @author logongas
 */
public class DataSessionImpl implements DataSession {

    Map<String,Object> sessions;
    DataSessionFactory dataSessionFactory;
    
    public DataSessionImpl(Map<String,Object> sessions,DataSessionFactory dataSessionFactory) {
        this.sessions=sessions;
        this.dataSessionFactory=dataSessionFactory;
    }

    
    @Override
    public Object getDataBaseSessionImpl() {
        return sessions.get("MAIN");
    }

    @Override
    public Object getDataBaseSessionAlternativeImpl() {
        return sessions.get("SECONDARY");
    }

    @Override
    public Object getSessionImpl(String name) {
        return sessions.get(name);
    }

    @Override
    public void close() throws Exception {
        this.dataSessionFactory.close(this);
    }

    
}
