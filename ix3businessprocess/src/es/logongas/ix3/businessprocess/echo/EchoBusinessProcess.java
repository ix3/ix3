/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
