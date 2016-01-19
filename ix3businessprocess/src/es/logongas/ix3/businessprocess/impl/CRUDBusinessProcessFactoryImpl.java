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
package es.logongas.ix3.businessprocess.impl;

import es.logongas.ix3.businessprocess.CRUDBusinessProcess;
import es.logongas.ix3.businessprocess.CRUDBusinessProcessFactory;
import es.logongas.ix3.util.FactoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Lorenzo
 */
public class CRUDBusinessProcessFactoryImpl implements CRUDBusinessProcessFactory {

    private String domainBasePackageName;
    private String interfaceBasePackageName;
    private String implBasePackageName;
    private String implSubPackageName = "impl";
    private String interfaceSufix = "CRUDBusinessProcess";
    private String implSufix = "CRUDBusinessProcessImpl";
    private final Class<? extends CRUDBusinessProcess> defaultImplClass = CRUDBusinessProcessImpl.class;

    @Autowired
    private ApplicationContext context;

    @Override
    public     <T> CRUDBusinessProcess<T,Integer> getBusinessProcess(Class<T> entityClass) {
        FactoryHelper<CRUDBusinessProcess> factoryHelper = new FactoryHelper<CRUDBusinessProcess>(domainBasePackageName, interfaceBasePackageName, implBasePackageName, implSubPackageName, interfaceSufix, implSufix, defaultImplClass, context);

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

    /**
     * @return the implSubPackageName
     */
    public String getImplSubPackageName() {
        return implSubPackageName;
    }

    /**
     * @param implSubPackageName the implSubPackageName to set
     */
    public void setImplSubPackageName(String implSubPackageName) {
        this.implSubPackageName = implSubPackageName;
    }

    /**
     * @param interfaceSufix the interfaceSufix to set
     */
    public void setInterfaceSufix(String interfaceSufix) {
        this.interfaceSufix = interfaceSufix;
    }

    /**
     * @param implSufix the implSufix to set
     */
    public void setImplSufix(String implSufix) {
        this.implSufix = implSufix;
    }

}
