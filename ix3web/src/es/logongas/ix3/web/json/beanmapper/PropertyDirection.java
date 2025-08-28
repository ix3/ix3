/*
 * FPempresa Copyright (C) 2025 Lorenzo González
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package es.logongas.ix3.web.json.beanmapper;

/**
 *
 * @author logongas
 */
public enum PropertyDirection {
    IN_TO_SERVER(true,false),
    OUT_FROM_SERVER(false,true),
    IN_TO_SERVER_OUT_FROM_SERVER(true,true),
    NONE(false,false);
    
    private boolean in;
    private boolean out;
    
    
    private PropertyDirection(boolean in,boolean out) {
        this.in=in;
        this.out=out;
    }
    
    public boolean isInToServer() {
        return in;
    }
    
    public boolean isOutFromServer() {
        return out;
    }
    
    public PropertyDirection join(PropertyDirection propertyDirection) {
        if (this==NONE) {
            return propertyDirection;
        } else if (this==IN_TO_SERVER_OUT_FROM_SERVER) {
            return IN_TO_SERVER_OUT_FROM_SERVER;
        } else if (this==IN_TO_SERVER) {
            switch (propertyDirection) {
                case IN_TO_SERVER:
                    return IN_TO_SERVER;
                case OUT_FROM_SERVER:
                    return IN_TO_SERVER_OUT_FROM_SERVER;
                case IN_TO_SERVER_OUT_FROM_SERVER:
                    return IN_TO_SERVER_OUT_FROM_SERVER;
                case NONE:
                    return IN_TO_SERVER;
                default:
                    throw new RuntimeException("Error de lógica:" + this + "," + propertyDirection);
            }
        } else if (this==OUT_FROM_SERVER) {
            switch (propertyDirection) {
                case IN_TO_SERVER:
                    return IN_TO_SERVER_OUT_FROM_SERVER;
                case OUT_FROM_SERVER:
                    return OUT_FROM_SERVER;
                case IN_TO_SERVER_OUT_FROM_SERVER:
                    return IN_TO_SERVER_OUT_FROM_SERVER;
                case NONE:
                    return OUT_FROM_SERVER;
                default:
                    throw new RuntimeException("Error de lógica:" + this + "," + propertyDirection);
            }
        } else {
            throw new RuntimeException("Error de lógica:" + this + "," + propertyDirection);
        }
        
        
    }
    
}
