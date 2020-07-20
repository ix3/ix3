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

package es.logongas.ix3.dao.impl.rules;

import es.logongas.ix3.rule.RuleContext;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.rule.impl.RuleContextImpl;

/**
 *
 * @author logongas
 * @param <T>
 */
public class RuleContextImplNoPrincipal<T> extends RuleContextImpl<T> implements RuleContext<T> {


    public RuleContextImplNoPrincipal(T entity, T originalEntity) {
        super(entity, originalEntity, null);
    }

    /**
     * @return the principal
     */
    @Override
    public Principal getPrincipal() {
        throw new RuntimeException("Estas reglas de negocio no permiten usar el 'principal'");
    }    
    
}
