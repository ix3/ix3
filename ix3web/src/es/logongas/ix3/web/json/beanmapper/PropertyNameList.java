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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author logongas
 */
public class PropertyNameList {

    private final Map<String,PropertyDirection> properties;
    
    public PropertyNameList(String propertyNames) {
        this(propertyNames, null);
    }
    public PropertyNameList(String propertyNames,String prefix) {
        this.properties=getPropertiesAndDirectionFromString(propertyNames,prefix);
    }    
    
    public PropertyDirection getPropertyDirection(String propertyName) {
        for (Map.Entry<String, PropertyDirection> entry : properties.entrySet()) {
            String startPropertyName = entry.getKey();        
        
            if (propertyName.equals(startPropertyName)) {
                return this.properties.get(startPropertyName);
            }
 
        }


        return PropertyDirection.NONE;
    }
    
    public PropertyNameList appendOrReplace(PropertyNameList newPropertyNameList) {
        List<String> propertyNamesFiltered=newPropertyNameList.getPropertyNamesFiltered(null, null);
        
        
        for(String propertyName:propertyNamesFiltered) {
            PropertyDirection propertyDirection = newPropertyNameList.getPropertyDirection(propertyName);
            
            properties.put(propertyName, propertyDirection);
        }
        
        return this;
    }    
    public PropertyNameList appendOrReplace(String rawPropertys) {
        PropertyNameList newPropertyNameList=new PropertyNameList(rawPropertys);
        
        this.appendOrReplace(newPropertyNameList);
        
        return this;
    } 
    
    public PropertyNameList remove(String propertyNames) {
        if ((propertyNames==null) && (propertyNames.trim().isEmpty()==true)) {
            return this;
        }
        
        
        if (propertyNames.contains(">")==true) {
            throw new RuntimeException("Al borrar no se puede indicar la dirección >:"+propertyNames);
        }  
        if (propertyNames.contains("<")==true) {
            throw new RuntimeException("Al borrar no se puede indicar la dirección <:"+propertyNames);
        } 
        
        String[] arrProperties = propertyNames.replace(" ", "").split(",");
        for (String rawProperty : arrProperties) {
            String propertyName = getPropertyNameFromRawPropertyName(rawProperty);

            if (properties.containsKey(propertyName)) {
                properties.remove(propertyName);
            }
        }
            
        return this;
    } 
        
    
    
    public List<String> getPropertyNamesFiltered(Boolean isInToServer,Boolean isOutFromServer) {
        List<String> propertyNamesFiltered=new ArrayList<>();
        
        for (Map.Entry<String, PropertyDirection> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            PropertyDirection propertyDirection = entry.getValue();
            
            if ((isInToServer!=null) && (isOutFromServer!=null)) {
                if ((propertyDirection.isInToServer()==isInToServer) && (propertyDirection.isOutFromServer()==isOutFromServer)) {
                    propertyNamesFiltered.add(propertyName);
                }
            } else if ((isInToServer!=null) && (isOutFromServer==null)) {
                if (propertyDirection.isInToServer()==isInToServer) {
                    propertyNamesFiltered.add(propertyName);
                }
            } else if ((isInToServer==null) && (isOutFromServer!=null)) {
                if (propertyDirection.isOutFromServer()==isOutFromServer) {
                    propertyNamesFiltered.add(propertyName);
                }
            } else if ((isInToServer==null) && (isOutFromServer==null)) {
                propertyNamesFiltered.add(propertyName);
            } else {
                throw new RuntimeException("Error de lógica:"+isInToServer+","+isOutFromServer);
            }
        }
        
        return propertyNamesFiltered;
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        
        boolean primeraVuelta = true;
        for (Map.Entry<String, PropertyDirection> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            PropertyDirection propertyDirection = entry.getValue();
            
            
            if (primeraVuelta==true) {
                primeraVuelta = false;
            } else {
                sb.append(",");
            }


            sb.append(joinPropertyNameAndDirection(propertyName, propertyDirection));
        }
        
        return sb.toString();
    }
    
    private Map<String,PropertyDirection> getPropertiesAndDirectionFromString(String propertyNames,String prefix) {
        Map<String,PropertyDirection>  properties=new LinkedHashMap<>();
        
        if ((propertyNames != null) && (propertyNames.trim().isEmpty() == false)) {         
        
        
            String[] arrProperties = propertyNames.replace(" ", "").split(",");
            for (String rawProperty : arrProperties) {
                String propertyName = getPropertyNameFromRawPropertyName(rawProperty);
                
                if ((prefix!=null) && (prefix.trim().isEmpty()==false)) {
                    propertyName=prefix+"."+propertyName;
                }
                
                PropertyDirection propertyDirection=getPropertyDirectionFromRawPropertyName(rawProperty);

                
                if (properties.containsKey(propertyName)) {
                    properties.put(propertyName, propertyDirection.join(properties.get(propertyName)));
                } else {
                    properties.put(propertyName, propertyDirection);
                }                
                
            }
        }
            
        return properties;
    }    
    
    private String getPropertyNameFromRawPropertyName(String rawProperty) {
        return rawProperty.replace(">", "").replace("<", "");
    }
    
    private PropertyDirection getPropertyDirectionFromRawPropertyName(String rawProperty) {
        PropertyDirection propertyDirection;
        if ((rawProperty.startsWith("<") == true) && (rawProperty.endsWith(">") == true)) {
            propertyDirection=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        } else if ((rawProperty.startsWith("<") == true) && (rawProperty.endsWith(">") == false)) {
            propertyDirection=PropertyDirection.OUT_FROM_SERVER;
        } else if ((rawProperty.startsWith("<") == false) && (rawProperty.endsWith(">") == true)) {
            propertyDirection=PropertyDirection.IN_TO_SERVER;
        } else if ((rawProperty.startsWith("<") == false) && (rawProperty.endsWith(">") == false)) {
            propertyDirection=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        } else {
            throw new RuntimeException("Error de logica:" + rawProperty.startsWith("<") + " , " + rawProperty.endsWith(">"));
        }
        
        return propertyDirection;

    }    
    
    
    private String joinPropertyNameAndDirection(String propertyName,PropertyDirection propertyDirection) {
        if (null==propertyDirection)  {
            throw new RuntimeException("propertyDirection inválida:"+propertyDirection);
        } else switch (propertyDirection) {
            case IN_TO_SERVER:
                return propertyName+">";
            case OUT_FROM_SERVER:
                return "<"+propertyName;
            case IN_TO_SERVER_OUT_FROM_SERVER:
                return propertyName;
                                
            default:
                throw new RuntimeException("propertyDirection inválida:"+propertyDirection);
        }
    }    
    
}
