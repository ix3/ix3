/*
 * Copyright 2014 Lorenzo.
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

import es.logongas.ix3.core.EntityType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.springframework.context.ApplicationContext;

/**
 * clase que ayuda a crear factorias facilmente para las clase de negocio.
 *
 * @author Lorenzo
 * @param <T>
 */
public class FactoryHelper<T> {

    private final String domainBasePackageName;
    private final String interfaceBasePackageName;
    private final String implBasePackageName;
    private final String interfaceSufix;
    private final String implSufix;
    private final Class<? extends T> defaultImplClass;
    private final ApplicationContext context;
    private final String implSubPackageName;

    public FactoryHelper(String domainBasePackageName, String interfaceBasePackageName, String implBasePackageName,String implSubPackageName, String interfaceSufix, String implSufix, Class<? extends T> defaultImplClass, ApplicationContext context) {
        this.domainBasePackageName = domainBasePackageName;
        this.interfaceBasePackageName = interfaceBasePackageName;
        this.implBasePackageName = implBasePackageName;
        this.implSubPackageName = implSubPackageName;
        this.interfaceSufix = interfaceSufix;
        this.implSufix = implSufix;
        this.defaultImplClass = defaultImplClass;
        this.context = context;
    }

    /**
     * Obtiene la Implementación de un objeto asociado a una clase de negocio.
     * El DAO debe tener el nombre siguiente DAONombreEntidad<<ImplSufi>>. Si no
     * existe una clase específica con ese nombre se retornará
     * GenericDAOImplHibernate. Hay úncamente 3 paquetes donde debe estar la
     * clase DAONombreEntidadImplHibernate En el paquete
     * 'interfaceBasePackageName' , en el paquete interfaceBasePackageName y un
     * subpaquete igual a subtituir domainBasePackageName por
     * interfaceBasePackageName o en un subpaquete del interfaz llamado
     * "implSubPackageName"
     *
     * @param entityClass
     * @return El DAO de la entidad
     */
    public T getImpl(Class entityClass) {
        //Hay 3 formas de encontrar el DAO
        String fqcn;
        T t;
        Class tClass;

        try {
            fqcn = getFQCNImplInSpecificPackage(entityClass, domainBasePackageName, implBasePackageName);
            tClass = Class.forName(fqcn);
            t = (T) context.getAutowireCapableBeanFactory().createBean(tClass);
        } catch (ClassNotFoundException ex) {
            //Si no existe probamos con la siguiente
            try {
                fqcn = getFQCNImplInSamePackage(entityClass, implBasePackageName);
                tClass = Class.forName(fqcn);
                t = (T) context.getAutowireCapableBeanFactory().createBean(tClass);
            } catch (ClassNotFoundException ex1) {
                try {
                    fqcn = getFQCNImplInSubPackage(entityClass, domainBasePackageName, implBasePackageName);
                    tClass = Class.forName(fqcn);
                    t = (T) context.getAutowireCapableBeanFactory().createBean(tClass);
                } catch (ClassNotFoundException ex2) {

                    
                    if (defaultImplClass==null) {
                        throw new RuntimeException("No se ha encontrado la implementación y no había una implementación por defecto"+entityClass.getName());
                    }
                    
                    Object bean;
                    try {
                        bean = context.getAutowireCapableBeanFactory().createBean(defaultImplClass);

                        Object noProxyBean=unProxyObject(bean);
                        if (noProxyBean instanceof EntityType) {
                            EntityType entityType=(EntityType)noProxyBean;
                            entityType.setEntityType(entityClass);
                        }
                        
                    } catch (Exception ex3) {
                        throw new RuntimeException(ex3);
                    }
                    
                    
                    //Pero como es generico deberemos ver si existe el interfaz
                    Class<? extends T> interfaceClass = getInterface(entityClass);
                    if (interfaceClass == null) {
                        //Si no existe el interfaz no hace falta crear el Proxy pq
                        //sería perder rendimiento.
                        t = (T) bean;
                    } else {
                        t = (T) Proxy.newProxyInstance(InvocationHandlerImpl.class.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandlerImpl(bean));
                    }
                }
            }
        }

        return t;
    }

    /**
     * Busca el interfaz del objeto que estamos fabricando relativo a la clase
     * de negocio. Si éste no existe retorna <code>null</code>
     *
     * @param entityClass La clase Java de una "Clase de negocio"
     * @return El interfaz
     */
    private Class<? extends T> getInterface(Class entityClass) {
        //Hay 3 formas de encontrar el interface
        String fqcn;
        Class interfaceClass;

        try {
            fqcn = getFQCNInterfaceInSpecificPackage(entityClass, domainBasePackageName, interfaceBasePackageName);
            interfaceClass = Class.forName(fqcn);
        } catch (Exception ex) {
            //Si no existe probamos con la siguiente
            try {
                fqcn = getFQCNInterfaceInSamePackage(entityClass, interfaceBasePackageName);
                interfaceClass = Class.forName(fqcn);
            } catch (Exception ex1) {
                //No existe es uqe no hay un interfaz concreto
                interfaceClass = null;
            }
        }

        return interfaceClass;
    }

    private String getFQCNInterfaceInSamePackage(Class entityClass, String implBasePackageName) {
        if (implBasePackageName != null) {
            String packageName = implBasePackageName;
            return packageName + "." + getInterfaceClassName(entityClass);
        } else {
            return null;
        }
    }

    private String getFQCNInterfaceInSpecificPackage(Class entityClass, String domainBasePackageName, String interfaceBasePackageName) {
        if ((domainBasePackageName != null) && (interfaceBasePackageName != null)) {
            String packageName = entityClass.getPackage().getName().replace(domainBasePackageName, interfaceBasePackageName);
            return packageName + "." + getInterfaceClassName(entityClass);
        } else {
            return null;
        }

    }

    private String getFQCNImplInSamePackage(Class entityClass, String implBasePackageName) {
        if (implBasePackageName != null) {
            String packageName = implBasePackageName;
            return packageName + "." + getImplClassName(entityClass);
        } else {
            return null;
        }
    }

    private String getFQCNImplInSpecificPackage(Class entityClass, String domainBasePackageName, String implBasePackageName) {
        if ((domainBasePackageName != null) && (implBasePackageName != null)) {
            String packageName = entityClass.getPackage().getName().replace(domainBasePackageName, implBasePackageName);
            return packageName + "." + getImplClassName(entityClass);
        } else {
            return null;
        }

    }

    private String getFQCNImplInSubPackage(Class entityClass, String domainBasePackageName, String implBasePackageName) {
        if ((domainBasePackageName != null) && (implBasePackageName != null)) {
            String packageName = entityClass.getPackage().getName().replace(domainBasePackageName, implBasePackageName) + "." + implSubPackageName;
            return packageName + "." + getImplClassName(entityClass);
        } else {
            return null;
        }

    }

    private String getInterfaceClassName(Class entityClass) {
        return entityClass.getSimpleName() + interfaceSufix;
    }

    private String getImplClassName(Class entityClass) {
        return entityClass.getSimpleName() + implSufix;
    }

    /**
     * Obtiene el objeto original de un objeto si este es un proxy. Si el objeto no está en un proxy retorna el mismo objeto.
     * @param proxyObject El  proxy
     * @return El objeto que contiene el proxy
     */
    private Object unProxyObject(Object proxyObject) {
        try {

            if (proxyObject == null) {
                return null;
            }

            Method methodGetTargetSource = ReflectionUtil.getMethod(proxyObject.getClass(), "getTargetSource");
            if (methodGetTargetSource != null) {
                Object targetSource = methodGetTargetSource.invoke(proxyObject);
                
                if (targetSource==null) {
                    return proxyObject;
                }
                
                Method methodGetTarget = ReflectionUtil.getMethod(targetSource.getClass(), "getTarget");
                if (methodGetTarget != null) {
                    return unProxyObject(methodGetTarget.invoke(targetSource));
                } else {
                    return proxyObject;
                }
            } else {
                return proxyObject;
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
