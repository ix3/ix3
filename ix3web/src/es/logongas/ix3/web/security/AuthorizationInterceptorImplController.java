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
package es.logongas.ix3.web.security;

import es.logongas.ix3.security.authorization.AuthorizationInterceptor;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import es.logongas.ix3.security.authorization.BusinessSecurityException;
import es.logongas.ix3.security.util.PrincipalLocator;
import es.logongas.ix3.web.controllers.command.CommandResult;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Comprueba si un Controlador puede ser ejecutado por un "Principal"
 * @author logongas
 */
public class AuthorizationInterceptorImplController implements AuthorizationInterceptor {
      
    private static final String SECURE_RESOURCE_TYPE_NAME = "Controller";
    private static final String PERMISSION_NAME_PRE_EXECUTE_CONTROLLER = "PreExecuteController";
    private static final String PERMISSION_NAME_POST_EXECUTE_CONTROLLER = "PostExecuteController";

    
    @Autowired
    AuthorizationManager authorizationManager;
    
    @Autowired
    PrincipalLocator principalLocator;
    
    public void checkPreAuthorized(Object obj,String methodName,Class entityType,Map<String,Object> arguments) throws BusinessSecurityException {
        if (arguments==null) {
            arguments=new HashMap<String,Object>();            
        }
        
        boolean isAuthorized=checkAuthorized(obj, PERMISSION_NAME_PRE_EXECUTE_CONTROLLER, methodName, entityType, arguments);
        
        if (isAuthorized==false) {
            throw new BusinessSecurityException("No tienes permiso para pre-ejecutar el controlador:"+getSecureResource(obj, methodName, entityType));
        }        
        
    }
    public void checkPostAuthorized(Object obj,String methodName,Class entityType,Map<String,Object> arguments,CommandResult commandResult) throws BusinessSecurityException {
        if (arguments==null) {
            arguments=new HashMap<String,Object>();            
        }
        
        boolean isAuthorized=checkAuthorized(obj, PERMISSION_NAME_POST_EXECUTE_CONTROLLER, methodName, entityType, new PostArguments(arguments, commandResult));
        
        if (isAuthorized==false) {
            throw new BusinessSecurityException("No tienes permiso para post-ejecutar el controlador:"+getSecureResource(obj, methodName, entityType));
        }        
        
    }    
    
    private boolean checkAuthorized(Object obj,String permissionName,String methodName,Class entityType,Object arguments) throws BusinessSecurityException {
        String secureResourceTypeName=SECURE_RESOURCE_TYPE_NAME;
        String secureResource=getSecureResource(obj, methodName, entityType);
        
        if (obj==null) {
            throw new IllegalArgumentException("El argumento obj no puede ser null");
        }
        if ((methodName==null) || (methodName.trim().isEmpty())) {
            throw new IllegalArgumentException("El argumento methodName no puede estar vacio");
        }        
        

        boolean isAuthorized=authorizationManager.authorized(principalLocator.getPrincipal(),secureResourceTypeName, secureResource, permissionName, arguments);
        
        return isAuthorized;
        
    } 


    
    private String getSecureResource(Object obj,String methodName,Class entityType) {
        String secureResource = obj.getClass().getSimpleName() + (entityType != null ? "." + entityType.getSimpleName() : "") + "." + methodName;

        return secureResource;
    }
    
}
