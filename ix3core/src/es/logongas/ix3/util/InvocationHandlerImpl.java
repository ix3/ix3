/*
 * Copyright 2013 Lorenzo González.
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
package es.logongas.ix3.util;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Esta clase es una ayuda para crear un Proxy de los interfaces deleganto la llamada a otro objeto.
 * @author Lorenzo González
 */
public class InvocationHandlerImpl implements InvocationHandler {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    Object realObject;

    public InvocationHandlerImpl(Object realObject) {
        this.realObject = realObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method realMethod = getRealMethod(realObject.getClass(), method);

        return realMethod.invoke(realObject, args);
    }

    private Method getRealMethod(Class realObjectClass, Method method) {
        Method[] methods = realObjectClass.getMethods();

        for (Method realMethod : methods) {
            if (realMethod.getName().equals(method.getName()) == true) {
                if (equalParameterTypes(realMethod.getParameterTypes(),method.getParameterTypes())) {
                    return realMethod;
                }
            }
        }

        //************* BEGIN:Quitar todo este código cuando se detecte el error *************************
        //A veces pasa que no encuentra el método así que si llegamos hasta aqui es que no lo ha encontrado
        //Así que vamos a añadir código para detectar que ha pasado.
        log.warn("Nombre clase:"+realObjectClass.getName());
        log.warn("método:" + method.toGenericString());        
        for (Method realMethod : methods) {
            if (realMethod.getName().equals(method.getName()) == true) {
                if (equalParameterTypes(realMethod.getParameterTypes(),method.getParameterTypes())) {
                    throw new RuntimeException("Se ha encontrado el método cuando antes no se había encontrado.");
                } else {
                   log.warn(realMethod.getName() + " no tiene los mismos parámetros que " + method.getName() + " ("  +realMethod.getParameterTypes().length + "," + method.getParameterTypes().length + ")"); 
                   if (realMethod.getParameterTypes().length==method.getParameterTypes().length) {
                        for(int i=0;i<realMethod.getParameterTypes().length;i++) {
                            if (method.getParameterTypes()[i].isAssignableFrom(realMethod.getParameterTypes()[i])==false) {
                                log.warn(method.getParameterTypes()[i].getName() + " no es del mismo tipo que " + realMethod.getParameterTypes()[i].getName()); 
                            }
                        }
                   }
                    
                }
            } else {
                log.warn("nombre método " + realMethod.getName() + " es distinto a " + method.getName());
            }
        }
        //************* END:Quitar todo este código cuando se detecte el error *************************
        
        
        throw new RuntimeException("No existe ese método:" + method.toGenericString());
    }

    private boolean equalParameterTypes(Class[] realParameterTypes,Class[] parameterTypes) {
        if (realParameterTypes.length!=parameterTypes.length) {
            return false;
        }

        for(int i=0;i<realParameterTypes.length;i++) {
            if (parameterTypes[i].isAssignableFrom(realParameterTypes[i])==false) {
                return false;
            }
        }

        return true;
    }

}
