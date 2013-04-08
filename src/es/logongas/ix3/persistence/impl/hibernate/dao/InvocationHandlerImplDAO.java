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
package es.logongas.ix3.persistence.impl.hibernate.dao;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Esta clase es una ayuda para crear un Proxy de los interfaces DAO.
 * @author Lorenzo González
 */
public class InvocationHandlerImplDAO implements InvocationHandler {

    Object realObject;

    public InvocationHandlerImplDAO(Object realObject) {
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

        throw new RuntimeException("No existe ese método:" + method.toGenericString());
    }

    private boolean equalParameterTypes(Class[] realParameterTypes,Class[] parameterTypes) {
        if (realParameterTypes.length!=parameterTypes.length) {
            return false;
        }

        for(int i=0;i<realParameterTypes.length;i++) {
            if (realParameterTypes[i].isAssignableFrom(parameterTypes[i])==false) {
                return false;
            }
        }

        return true;
    }

}
