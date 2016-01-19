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
package es.logongas.ix3.web.businessprocess;

import es.logongas.ix3.businessprocess.BusinessProcess;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.web.controllers.schema.Schema;
import es.logongas.ix3.web.json.beanmapper.Expands;

/**
 *
 * @author logongas
 */
public interface SchemaBusinessProcess {
    
    Schema getSchema(GetSchemaArguments getSchemaArguments) throws BusinessException ;
    
    public class GetSchemaArguments extends BusinessProcess.BusinessProcessArguments {
        
        public Class entityType;
        public Expands expands;

        public GetSchemaArguments() {
        }

        public GetSchemaArguments(Principal principal, DataSession dataSession, Class entityType, Expands expands) {
            super(principal, dataSession);
            this.entityType = entityType;
            this.expands = expands;
        }
        
        
        
    }
    
    
}
