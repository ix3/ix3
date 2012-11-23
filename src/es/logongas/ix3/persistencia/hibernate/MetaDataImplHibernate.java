/*
 * Copyright 2012 alumno.
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
package es.logongas.ix3.persistencia.hibernate;

import es.logongas.ix3.persistencia.dao.MetaData;

/**
 *
 * @author alumno
 */
public class MetaDataImplHibernate implements MetaData {

    Class entityType;
    
    protected MetaDataImplHibernate(Class entityType) {
        this.entityType=entityType;
    }
    
    
    @Override
    public Class getEntiyType() {
        return entityType;
    }
    
}
