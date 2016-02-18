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
