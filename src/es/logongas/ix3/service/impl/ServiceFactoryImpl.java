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

package es.logongas.ix3.service.impl;

import es.logongas.ix3.service.GenericService;
import es.logongas.ix3.service.ServiceFactory;
import es.logongas.ix3.util.FactoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Lorenzo
 */
public class ServiceFactoryImpl implements ServiceFactory {
    
    private String domainBasePackageName ;
    private String interfaceBasePackageName;
    private String implBasePackageName;
    private final String interfaceSufix="Service";
    private final String implSufix="ServiceImplHibernate"; 
    private final Class<? extends GenericService> defaultImplClass = GenericServiceImpl.class;
    
    @Autowired
    private ApplicationContext context;
    
    @Override
    public GenericService getService(Class entityClass) {
        FactoryHelper<GenericService> factoryHelper=new FactoryHelper<GenericService>(domainBasePackageName, interfaceBasePackageName, implBasePackageName, interfaceSufix, implSufix, defaultImplClass, context);
        
        return factoryHelper.getImpl(entityClass);
    }

    /**
     * @param domainBasePackageName the domainBasePackageName to set
     */
    public void setDomainBasePackageName(String domainBasePackageName) {
        this.domainBasePackageName = domainBasePackageName;
    }

    /**
     * @param interfaceBasePackageName the interfaceBasePackageName to set
     */
    public void setInterfaceBasePackageName(String interfaceBasePackageName) {
        this.interfaceBasePackageName = interfaceBasePackageName;
    }

    /**
     * @param implBasePackageName the implBasePackageName to set
     */
    public void setImplBasePackageName(String implBasePackageName) {
        this.implBasePackageName = implBasePackageName;
    }

}
