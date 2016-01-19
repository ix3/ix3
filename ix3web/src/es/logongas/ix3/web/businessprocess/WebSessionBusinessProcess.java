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
package es.logongas.ix3.web.businessprocess;

import es.logongas.ix3.businessprocess.BusinessProcess;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.service.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gestión de la sesión del usuario en la capa de Web
 * @author logongas
 */
public interface WebSessionBusinessProcess extends Service {
    
    Principal createWebSession(CreateWebSessionArguments createWebSessionArguments) throws BusinessException;
    void deleteCurrentWebSession(DeleteCurrentWebSessionArguments deleteCurrentWebSessionArguments) throws BusinessException;
    Principal getCurrentWebSession(GetCurrentWebSessionArguments getCurrentWebSessionArguments) throws BusinessException;
    
    public class CreateWebSessionArguments extends BusinessProcess.BusinessProcessArguments {
        public HttpServletRequest httpServletRequest;
        public HttpServletResponse httpServletResponse;

        public CreateWebSessionArguments() {
        }

        public CreateWebSessionArguments(Principal principal, DataSession dataSession,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
            super(principal, dataSession);
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
        }
        
        
        
    }
    
    
    public class DeleteCurrentWebSessionArguments extends BusinessProcess.BusinessProcessArguments {
        public HttpServletRequest httpServletRequest;
        public HttpServletResponse httpServletResponse;
        
        public DeleteCurrentWebSessionArguments() {
        }

        public DeleteCurrentWebSessionArguments(Principal principal, DataSession dataSession,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
            super(principal, dataSession);
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
        }        
    }
    
    
    public class GetCurrentWebSessionArguments extends BusinessProcess.BusinessProcessArguments {
        public HttpServletRequest httpServletRequest;
        public HttpServletResponse httpServletResponse;
        
        public GetCurrentWebSessionArguments() {
        }

        public GetCurrentWebSessionArguments(Principal principal, DataSession dataSession,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
            super(principal, dataSession);
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
        }        
        
        
    }    
}
