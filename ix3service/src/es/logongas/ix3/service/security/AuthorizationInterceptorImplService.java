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
package es.logongas.ix3.service.security;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.security.authorization.AuthorizationInterceptor;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import es.logongas.ix3.security.util.PrincipalLocator;
import es.logongas.ix3.service.Service;
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
public class AuthorizationInterceptorImplService implements AuthorizationInterceptor {

    private static final String SECURE_RESOURCE_TYPE_NAME = "Service";
    private static final String PERMISSION_NAME_PRE_EXECUTE = "preexecute";
    private static final String PERMISSION_NAME_POST_EXECUTE = "postexecute";

    @Autowired
    AuthorizationManager authorizationManager;

    @Autowired
    PrincipalLocator principalLocator;

    /**
     * Se ejecuta ANTES de cualquier llamada a una clase que herede del interfaz "Service" y comprueba la seguridad de si se puede llamar al método
     * @param joinPoint
     * @throws BusinessException 
     */
    @Before("within(es.logongas.ix3.service.Service+)")
    public void checkPreAuthorized(JoinPoint joinPoint) throws BusinessException {
        Principal principal = principalLocator.getPrincipal();
        String secureResource = getSecureResource(joinPoint);
        Object[] arguments = getArguments(joinPoint);

        boolean isAuthorized = authorizationManager.authorized(principal, SECURE_RESOURCE_TYPE_NAME, secureResource, PERMISSION_NAME_PRE_EXECUTE, arguments);

        if (isAuthorized == false) {
            throw new BusinessException("No tiene permisos para ejecutar el método:" + SECURE_RESOURCE_TYPE_NAME + ":" + secureResource);
        }

    }

    /**
     * Se ejecuta DESPUES de cualquier llamada a una clase que herede del interfaz "Service" y comprueba la seguridad de si se puede retornar los datos del método
     * @param joinPoint
     * @param result
     * @throws BusinessException 
     */
    @AfterReturning(pointcut = "within(es.logongas.ix3.service.Service+)", returning = "result")
    public void checkPostAuthorized(JoinPoint joinPoint, Object result) throws BusinessException {
        Principal principal = principalLocator.getPrincipal();
        String secureResource = getSecureResource(joinPoint);
        Object[] arguments = new Object[]{result};

        boolean isAuthorized = authorizationManager.authorized(principal, SECURE_RESOURCE_TYPE_NAME, secureResource, PERMISSION_NAME_POST_EXECUTE, arguments);

        if (isAuthorized == false) {
            throw new BusinessException("No tiene permisos para devolver los datos del método:" + SECURE_RESOURCE_TYPE_NAME + ":" + secureResource);
        }

    }

    private Object[] getArguments(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();

        return arguments;
    }

    private String getSecureResource(JoinPoint joinPoint) {
        Service service = (Service) joinPoint.getTarget();
        String methodName = joinPoint.getSignature().getName();

        String secureResource = getInterfaceService(service).getName() + (service.getEntityType() != null ? "." + service.getEntityType().getSimpleName() : "") + "." + methodName;

        return secureResource;
    }

    /**
     * Dado un objeto que implementa un interfaz Service o alguno que hereda de
     * él, obtiene el que exactamente implementa.
     *
     * @param service objeto que implementa un interfaz Service o alguno que
     * hereda de él
     * @return Exactamente el interfaz que implementa
     */
    private Class<? extends Service> getInterfaceService(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("El objeto service no puede ser null");
        }

        Class clazz = service.getClass();

        Class[] interfaces = clazz.getInterfaces();

        Class<? extends Service> interfaceService = null;
        for (Class interfaze : interfaces) {
            if (Service.class.isAssignableFrom(interfaze) == true) {

                if (interfaceService != null) {
                    throw new RuntimeException("El objeto de la clase " + service.getClass().getName() + " no puede implementar mas de 2 interfaces Service pero implementa al menos " + interfaceService.getName() + " y " + interfaze.getName());
                }

                interfaceService = interfaze;

            }
        }

        return interfaceService;
    }

}
