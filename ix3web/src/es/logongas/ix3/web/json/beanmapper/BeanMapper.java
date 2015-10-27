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
 * Como transformar los datos que vienen desde la Web (Json) al bean y viceversa. Realmente lo que dice es que campos quitar en cada transformacion (toJson y toObject)
 *
 * @author logongas
 */
public final class BeanMapper {

    private final static Pattern deletePropertiesPattern = Pattern.compile("(\\<?[_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)\\>?(,\\<?([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)\\>?)*");
    private final static Pattern expandPropertiesPattern = Pattern.compile("(\\<?([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\<?\\*)\\>?(,\\<?(([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\*)\\>?)*");

    private final Class entityClass;

    private final List<String> inDeleteProperties;
    private final List<String> outDeleteProperties;

    private final List<String> inExpandProperties;
    private final List<String> outExpandProperties;

    public BeanMapper(Class entityClass) {
        this(entityClass, null, null);
    }

    public BeanMapper(Class entityClass, String deleteProperties, String expandProperties) {

        this.entityClass = entityClass;

        this.inDeleteProperties = new ArrayList<String>();
        this.outDeleteProperties = new ArrayList<String>();
        populateInOutLists(deleteProperties,deletePropertiesPattern,inDeleteProperties,outDeleteProperties);


        this.inExpandProperties = new ArrayList<String>();
        this.outExpandProperties = new ArrayList<String>();
        populateInOutLists(expandProperties,expandPropertiesPattern,inExpandProperties,outExpandProperties);
        

        this.validate();
    }

    private void populateInOutLists(String properties,Pattern pattern,List<String> in,List<String> out) {
        if ((properties != null) && (properties.trim().isEmpty() == false)) {
            if (pattern.matcher(properties).matches() == false) {
                throw new RuntimeException("El parámetro properties no tiene el formato adecuado:" + properties + " , " + pattern.pattern());
            }
            
            String[] arrProperties=properties.split(",");
            for(String rawProperty:arrProperties) {
                String propertyName=rawProperty.replace(">","").replace("<","");
                if ((rawProperty.startsWith("<")==true) && (rawProperty.endsWith(">")==true)) {
                    in.add(propertyName);
                    out.add(propertyName);
                } else if ((rawProperty.startsWith("<")==true) && (rawProperty.endsWith(">")==false)) {
                    out.add(propertyName);
                } else if ((rawProperty.startsWith("<")==false) && (rawProperty.endsWith(">")==true)) {
                    in.add(propertyName);
                } else if ((rawProperty.startsWith("<")==false) && (rawProperty.endsWith(">")==false)) {
                    in.add(propertyName);
                    out.add(propertyName);
                } else {
                    throw new RuntimeException("Error de logica:" + rawProperty.startsWith("<") + " , " + rawProperty.endsWith(">"));
                }
            }
            
        }
        
        
    }



    public boolean isExpandInProperty(String propertyNameExpand) {
        if (expandMath(inExpandProperties, propertyNameExpand)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpandOutProperty(String propertyNameExpand) {
        if (expandMath(outExpandProperties, propertyNameExpand)) {
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
        for (String propertyName : inDeleteProperties) {
            if (propertyName.equals(propertyNameDelete)) {
                return true;
            }
        }

        return false;
    }

    public boolean isDeleteOutProperty(String propertyNameDelete) {
        for (String propertyName : outDeleteProperties) {
            if (propertyName.equals(propertyNameDelete)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compruba si los valores del BeanMapper son válidos. Hay que comprobarlo pq varios son de tipo String y solo se pueden comprobar en tiempo de ejecución mediante refection Si no son válidos lanza
     * directamente una expceción con todos los fallos que ha encontrado
     *
     * @return
     */
    public void validate() throws RuntimeException {
        StringBuilder sb = new StringBuilder();

        if (this.entityClass == null) {
            sb.append("No existe una clase para el ObjectMapper\n");
        } else {

            for (String propertyName : this.inDeleteProperties) {
                if (ReflectionUtil.existsWritePropertyInClass(entityClass, propertyName) == false) {
                    sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }
            for (String propertyName : this.outDeleteProperties) {
                if (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false) {
                    sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }

            for (String propertyName : this.inExpandProperties) {
                if (("*".equals(propertyName) == false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                    sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }
            for (String propertyName : this.outExpandProperties) {
                if (("*".equals(propertyName) == false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                    sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }

        }

        if (sb.length() > 0) {
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
