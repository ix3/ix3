/*
 * Copyright 2015 Lorenzo.
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
package es.logongas.ix3.businessprocess;

import es.logongas.ix3.core.Principal;
import es.logongas.ix3.dao.DataSession;

/**
 *
 * @author logongas
 */
public interface BusinessProcess<EntityType> extends es.logongas.ix3.core.EntityType<EntityType> {

    public class BusinessProcessArguments {

        public Principal principal;
        public DataSession dataSession;

        public BusinessProcessArguments() {
        }

        public BusinessProcessArguments(Principal principal, DataSession dataSession) {
            this.principal = principal;
            this.dataSession = dataSession;
        }
    }
}
