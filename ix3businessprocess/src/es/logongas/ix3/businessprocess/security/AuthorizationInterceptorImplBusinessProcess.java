/*
 * Copyright 2015 Lorenzo Gonzalez.
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
package es.logongas.ix3.businessprocess.security;

import es.logongas.ix3.businessprocess.BusinessProcess;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.DataSessionFactory;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.security.authorization.AuthorizationInterceptor;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import es.logongas.ix3.security.authorization.BusinessSecurityException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Clase que interepta todas las llamadas a un servicio
 *
 * @author logongas
 */
@Aspect
public class AuthorizationInterceptorImplBusinessProcess implements AuthorizationInterceptor {

    private static final String SECURE_RESOURCE_TYPE_NAME = "BusinessProcess";
    private static final String PERMISSION_NAME_PRE_EXECUTE = "PreExecuteBusinessProcess";
    private static final String PERMISSION_NAME_POST_EXECUTE = "PostExecuteBusinessProcess";

    @Autowired
    AuthorizationManager authorizationManager;

    @Autowired
    DataSessionFactory dataSessionFactory;

    /**
     * Se ejecuta ANTES de cualquier llamada a una clase que herede del interfaz "BusinessProcess" y comprueba la seguridad de si se puede llamar al método
     *
     * @param joinPoint
     * @throws BusinessException
     */
    @Before("within(es.logongas.ix3.businessprocess.BusinessProcess+)")
    public void checkPreAuthorized(JoinPoint joinPoint) throws Exception {
        Principal principal = getPrincipal(joinPoint);
        String secureResource = getSecureResource(joinPoint);
        Object arguments = getMethodArguments(joinPoint);

        boolean isAuthorized;
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            isAuthorized = authorizationManager.authorized(principal, SECURE_RESOURCE_TYPE_NAME, secureResource, PERMISSION_NAME_PRE_EXECUTE, arguments, dataSession);
        }
        if (isAuthorized == false) {
            throw new BusinessSecurityException("El usuario " + principal + " no tiene permiso para ejecutar el proceso de negocio:" + secureResource);
        }

    }

    /**
     * Se ejecuta DESPUES de cualquier llamada a una clase que herede del interfaz "BusinessProcess" y comprueba la seguridad de si se puede retornar los datos del método
     *
     * @param joinPoint
     * @param result
     * @throws BusinessException
     */
    @AfterReturning(pointcut = "within(es.logongas.ix3.businessprocess.BusinessProcess+)", returning = "result")
    public void checkPostAuthorized(JoinPoint joinPoint, Object result) throws Exception {
        Principal principal = getPrincipal(joinPoint);
        String secureResource = getSecureResource(joinPoint);
        PostArguments arguments = new PostArguments(getMethodArguments(joinPoint), result);

        boolean isAuthorized;
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            isAuthorized = authorizationManager.authorized(principal, SECURE_RESOURCE_TYPE_NAME, secureResource, PERMISSION_NAME_POST_EXECUTE, arguments, dataSession);
        }
        if (isAuthorized == false) {
            throw new BusinessSecurityException("El usuario " + principal + " no tiene permiso para devolver los datos del proceso de negocio:" + secureResource);
        }

    }

    private Object getMethodArguments(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        if ((arguments == null) ||(arguments.length != 1)) {
            throw new RuntimeException("El método no tiene ningún argumento o mas de uno:" + getSecureResource(joinPoint));
        }

        if (arguments[0]==null) {
            throw new RuntimeException("El argumento no puede ser null:" + getSecureResource(joinPoint));
        } 
        
        
        return arguments[0];
    }

    private Principal getPrincipal(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();

        if ((arguments == null) ||(arguments.length != 1)) {
            throw new RuntimeException("El método no tiene ningún argumento o mas de uno:" + getSecureResource(joinPoint));
        }

        if (arguments[0]==null) {
            throw new RuntimeException("El argumento no puede ser null:" + getSecureResource(joinPoint));
        }        
        
        if (BusinessProcess.BusinessProcessArguments.class.isAssignableFrom(arguments[0].getClass())==false) {
            throw new RuntimeException("El método no es de tipo BusinessProcessArguments:" + getSecureResource(joinPoint));
        }
 
        BusinessProcess.BusinessProcessArguments businessProcessArguments=(BusinessProcess.BusinessProcessArguments) arguments[0];
        
        return businessProcessArguments.principal;
        
    }

    private String getSecureResource(JoinPoint joinPoint) {
        BusinessProcess businessProcess = (BusinessProcess) joinPoint.getTarget();
        String methodName = joinPoint.getSignature().getName();

        String secureResource = getInterfaceBusinessProcess(businessProcess).getSimpleName() + (businessProcess.getEntityType() != null ? "." + businessProcess.getEntityType().getSimpleName() : "") + "." + methodName;

        return secureResource;
    }

    /**
     * Dado un objeto que implementa un interfaz BusinessProcess o alguno que hereda de él, obtiene el que exactamente implementa.
     *
     * @param businessProcess objeto que implementa un interfaz BusinessProcess o alguno que hereda de él
     * @return Exactamente el interfaz que implementa
     */
    private Class<? extends BusinessProcess> getInterfaceBusinessProcess(BusinessProcess businessProcess) {
        if (businessProcess == null) {
            throw new IllegalArgumentException("El objeto businessProcess no puede ser null");
        }

        Class clazz = businessProcess.getClass();

        Class[] interfaces = clazz.getInterfaces();

        Class<? extends BusinessProcess> interfaceBusinessProcess = null;
        for (Class interfaze : interfaces) {
            if (BusinessProcess.class.isAssignableFrom(interfaze) == true) {

                if (interfaceBusinessProcess != null) {
                    throw new RuntimeException("El objeto de la clase " + businessProcess.getClass().getName() + " no puede implementar mas de 2 interfaces BusinessProcess pero implementa al menos " + interfaceBusinessProcess.getName() + " y " + interfaze.getName());
                }

                interfaceBusinessProcess = interfaze;

            }
        }

        return interfaceBusinessProcess;
    }

}
