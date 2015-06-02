/*
 * Copyright 2015 Lorenzo Gonzalez.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.logongas.ix3.service.rules.impl;

import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.service.rules.RuleContext;
import es.logongas.ix3.util.ReflectionUtil;

/**
 * Para valores dentro de otros valores, permite evitar que se obtenga el valor
 * original y de esa forma evitando accesos a la base de datos.
 * Lo que se hace el pasar el RuleContext "padre" y se llama a él para obtener el valor original
 *
 * @author logongas
 */
public class RuleContextImplDelegate implements RuleContext {

    RuleContext parentRuleContext;
    String propertyName;

    public RuleContextImplDelegate(RuleContext parentRuleContext, String propertyName) {
        this.parentRuleContext = parentRuleContext;
        this.propertyName = propertyName;
    }

    @Override
    public Object getEntity() {
        Object entity;

        if (parentRuleContext.getEntity() != null) {
            entity = ReflectionUtil.getValueFromBean(parentRuleContext.getEntity(), propertyName);
        } else {
            entity = null;
        }

        return entity;
    }

    @Override
    public Object getOriginalEntity() {
        Object originalEntity;

        if (parentRuleContext.getOriginalEntity() != null) {
            originalEntity = ReflectionUtil.getValueFromBean(parentRuleContext.getOriginalEntity(), propertyName);
        } else {
            originalEntity = null;
        }

        return originalEntity;
    }

    @Override
    public Principal getPrincipal() {
        return parentRuleContext.getPrincipal();
    }

}
