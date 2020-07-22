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
package es.logongas.ix3.businessprocess.echo;

import es.logongas.ix3.businessprocess.BusinessProcess;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.dao.DataSession;

/**
 *
 * @author logongas
 */
public interface EchoBusinessProcess extends BusinessProcess {
    
    EchoResult echoDataBase(EchoDataBaseArguments echoDataBaseArguments);
    EchoResult echoNoDataBase(EchoNoDataBaseArguments echoNoDataBaseArguments);

    public class EchoDataBaseArguments extends BusinessProcessArguments {

        public long id;

        public EchoDataBaseArguments() {
        }

        public EchoDataBaseArguments(Principal principal, DataSession dataSession, long id) {
            super(principal, dataSession);
            this.id = id;
        }
    }
    
    public class EchoNoDataBaseArguments extends BusinessProcessArguments {

        public EchoNoDataBaseArguments() {
        }

        public EchoNoDataBaseArguments(Principal principal, DataSession dataSession) {
            super(principal, dataSession);
        }
    }    
    
}
