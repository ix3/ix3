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

import es.logongas.ix3.util.ReflectionUtil;
import java.util.regex.Pattern;

/**
 * Como transformar los datos que vienen desde la Web (Json) al bean y viceversa. Realmente lo que dice es que campos quitar en cada transformacion (toJson y toObject)
 *
 * @author logongas
 */
public final class BeanMapper {

    private final static Pattern deletePropertiesPattern = Pattern.compile("(\\s*\\<?[_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)\\>?\\s*(\\s*,\\s*\\<?([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)\\>?)*\\s*");
    private final static Pattern expandPropertiesPattern = Pattern.compile("(\\s*\\<?([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\<?\\*)\\>?\\s*(\\s*,\\s*\\<?(([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\*)\\>?)*\\s*");

    private final Class entityClass;

    private final PropertyNameList deletePropertyNameList;
    private final PropertyNameList expandPropertyNameList;
    
    public BeanMapper(Class entityClass) {
        this(entityClass, null, null);
    }

    /**
     * Crea un nuevo objeto
     *
     * @param entityClass Nombre de la clase Java sobre la que se aplicará el BeanMapper
     * @param deleteProperties Propiedaes a borrar se paradas por comas. Si se inclue '&lt;' delante del nombre de la propiedad, solo se borrará desde el objeto hacia Json Si se inclue '&gt;' detras
     * del nombre de la propiedad, solo se borrará desde Json hacia el objeto
     * @param expandProperties Propiedaes a expandir se paradas por comas. Se permite el "*" para indicar que se expanden todas las propiedades. Si se inclue '&lt;' delante del nombre de la propiedad,
     * solo se expandirán desde el objeto hacia Json Si se inclue '&gt;' detras del nombre de la propiedad, solo se expandirán desde Json hacia el objeto
     */
    public BeanMapper(Class entityClass, String deleteProperties, String expandProperties) {

        this.entityClass = entityClass;

        if ((deleteProperties != null) && (deleteProperties.trim().isEmpty() == false)) {
            if (deletePropertiesPattern.matcher(deleteProperties).matches() == false) {
                throw new RuntimeException("El parámetro properties no tiene el formato adecuado:" + deleteProperties + " , " + deletePropertiesPattern.pattern());
            }
        }
        this.deletePropertyNameList=new PropertyNameList(deleteProperties, null);        
        
        
        if ((expandProperties != null) && (expandProperties.trim().isEmpty() == false)) {
            if (expandPropertiesPattern.matcher(expandProperties).matches() == false) {
                throw new RuntimeException("El parámetro properties no tiene el formato adecuado:" + expandProperties + " , " + expandPropertiesPattern.pattern());
            }
        }        
        this.expandPropertyNameList=new PropertyNameList(expandProperties, null);

        this.validate();
    }



    public boolean isExpandInProperty(String propertyNameExpand) {
        return Expands.isExpandProperty(propertyNameExpand, this.expandPropertyNameList.getPropertyNamesFiltered(Boolean.TRUE, null));
    }

    public boolean isExpandOutProperty(String propertyNameExpand) {
        return Expands.isExpandProperty(propertyNameExpand, this.expandPropertyNameList.getPropertyNamesFiltered(null,Boolean.TRUE));
    }

    public boolean isDeleteInProperty(String propertyNameDelete) {
        return this.deletePropertyNameList.getPropertyDirection(propertyNameDelete).isInToServer();
    }

    public boolean isDeleteOutProperty(String propertyNameDelete) {
        return this.deletePropertyNameList.getPropertyDirection(propertyNameDelete).isOutFromServer();
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

            for (String propertyName : this.deletePropertyNameList.getPropertyNamesFiltered(Boolean.TRUE,null)) {
                if (ReflectionUtil.existsWritePropertyInClass(entityClass, propertyName) == false) {
                    sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }
            for (String propertyName : this.deletePropertyNameList.getPropertyNamesFiltered(null,Boolean.TRUE)) {
                if (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false) {
                    sb.append("No existe la propiedad get de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }

            for (String propertyName : this.expandPropertyNameList.getPropertyNamesFiltered(Boolean.TRUE, null)) {
                if (("*".equals(propertyName) == false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                    sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }
            for (String propertyName : this.expandPropertyNameList.getPropertyNamesFiltered(null,Boolean.TRUE)) {
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
