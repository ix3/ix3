/*
 * ix3 Copyright 2020 Lorenzo González.
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
package es.logongas.ix3.rule.impl;

import es.logongas.ix3.rule.RuleEngine;
import es.logongas.ix3.rule.RuleEngineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author logongas
 */
public class RuleEngineFactoryImpl implements RuleEngineFactory {

    @Autowired
    ApplicationContext applicationContext;
    
    @Override
    public <T> RuleEngine<T> getRuleEngine(Class<T> clazz) {
        RuleEngine ruleEngine=new RuleEngineImpl<T>();
        
        applicationContext.getAutowireCapableBeanFactory().autowireBean(ruleEngine);
        
        return ruleEngine;
    }
    
}
