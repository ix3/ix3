/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.web.json.beanmapper;

import es.logongas.ix3.core.BusinessMessage;
import es.logongas.ix3.util.ReflectionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Como transformar los datos que vienen desde la Web (Json) al bean y
 * viceversa. Realmente lo que dice es que campos quitar en cada transformacion
 * (toJson y toObject)
 *
 * @author logongas
 */
public class BeanMapper {

    private final Pattern expandsPattern = Pattern.compile("(([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\*)(,(([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\*))*");
    private final Pattern propertiesPattern = Pattern.compile("([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)(,([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*))*");

    private final Class entityClass;

    private final List<String> listInOutDeleteProperties;
    private final List<String> listInDeleteProperties;
    private final List<String> listOutDeleteProperties;

    private final List<String> listInOutExpands;
    private final List<String> listInExpands;
    private final List<String> listOutExpands;


    public BeanMapper(Class entityClass) {
        this(entityClass,null,null,null,null,null,null);
    }    
    
    
    public BeanMapper(Class entityClass, String inOutDeleteProperties, String inDeleteProperties, String outDeleteProperties, String inOutExpands, String inExpands, String outExpands) {       
        this.entityClass = entityClass;

        this.listInOutDeleteProperties = getProperties(inOutDeleteProperties);
        this.listInDeleteProperties = getProperties(inDeleteProperties);
        this.listOutDeleteProperties = getProperties(outDeleteProperties);

        this.listInOutExpands = getExpands(inOutExpands);
        this.listInExpands = getExpands(inExpands);
        this.listOutExpands = getExpands(outExpands);
        
        this.validate();
    }

    private List<String> getExpands(String expands) {
        if ((expands == null) || (expands.trim().isEmpty())) {
            return new ArrayList<String>();
        } else {
        if (expandsPattern.matcher(expands).matches() == false) {
            throw new RuntimeException("El parámetro expand no tiene el formato adecuado: '" + expands + "' , " + expandsPattern.pattern());
        }            
            
            return Arrays.asList(expands.split(","));
        }
    }

    private List<String> getProperties(String properties) {
        if ((properties == null) || (properties.trim().isEmpty())) {
            return new ArrayList<String>();
        } else {
        if (propertiesPattern.matcher(properties).matches() == false) {
            throw new RuntimeException("El parámetro expand no tiene el formato adecuado:" + properties + " , " + expandsPattern.pattern());
        }            
            
            return Arrays.asList(properties.split(","));
        }
    }

    public boolean isExpandInProperty(String propertyNameExpand) {
        if (expandMath(listInOutExpands,propertyNameExpand)) {
            return true;
        } else if (expandMath(listInExpands,propertyNameExpand)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpandOutProperty(String propertyNameExpand) {
        if (expandMath(listInOutExpands,propertyNameExpand)) {
            return true;
        } else if (expandMath(listOutExpands,propertyNameExpand)) {
            return true;
        } else {
            return false;
        }
    }

    
    private boolean expandMath(List<String> expands, String propertyNameExpand) {
        for (String propertyName : expands) {
            if ((propertyName.trim()).startsWith(propertyNameExpand + ".") || (propertyName.trim().equals(propertyNameExpand)) || (propertyName.trim().equals("*"))) {
                return true;
            }
        }

        return false;

    }
    
    public boolean isDeleteInProperty(String propertyNameDelete) {
        for (String propertyName : listInOutDeleteProperties) {
            if (propertyName.equals(propertyNameDelete)) {
                return true;
            }
        }
        for (String propertyName : listInDeleteProperties) {
            if (propertyName.equals(propertyNameDelete)) {
                return true;
            }
        }
        
        return false;
    }

    public boolean isDeleteOutProperty(String propertyNameDelete) {

        for (String propertyName : listInOutDeleteProperties) {
            if (propertyName.equals(propertyNameDelete)) {
                return true;
            }
        }
        for (String propertyName : listOutDeleteProperties) {
            if (propertyName.equals(propertyNameDelete)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Compruba si los valores del BeanMapper son válidos. Hay que comprobarlo
     * pq varios son de tipo String y solo se pueden comprobar en tiempo de
     * ejecución mediante refection Si no son válidos lanza directamente una
     * expceción con todos los fallos que ha encontrado
     *
     * @return
     */
    public void validate() throws RuntimeException {
        StringBuilder sb=new StringBuilder();

        if (this.entityClass == null) {
            sb.append("No existe una clase para el ObjectMapper\n");
        } else {


                for (String propertyName : this.listInDeleteProperties) {
                    if (ReflectionUtil.existsWritePropertyInClass(entityClass, propertyName) == false) {
                        sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                }
                for (String propertyName : this.listOutDeleteProperties) {
                    if (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false) {
                        sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                }
                for (String propertyName : this.listInOutDeleteProperties) {
                    if (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false) {
                        sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                    if (ReflectionUtil.existsWritePropertyInClass(entityClass, propertyName) == false) {
                        sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                }
      
                
                for (String propertyName : this.listInExpands) {
                    if (("*".equals(propertyName)==false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                        sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                }
                for (String propertyName : this.listOutExpands) {
                    if (("*".equals(propertyName)==false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                        sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                }
                for (String propertyName : this.listInOutExpands) {
                    if (("*".equals(propertyName)==false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                        sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                    if (("*".equals(propertyName)==false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                        sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName()+"\n");
                    }
                }
                
        }

        if (sb.length()>0) {
            throw new IllegalArgumentException("Los datos del BeanMapper no son correctos:" + sb.toString());
        }

    }

    /**
     * @return the entityClass
     */
    public Class getEntityClass() {
        return entityClass;
    }

}
