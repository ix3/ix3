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
import java.util.ArrayList;
import java.util.List;
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

    private final List<String> inDeleteProperties;
    private final List<String> outDeleteProperties;

    private final Expands inExpands;
    private final Expands outExpands;

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

        this.inDeleteProperties = new ArrayList<String>();
        this.outDeleteProperties = new ArrayList<String>();
        populateInOutLists(deleteProperties, deletePropertiesPattern, inDeleteProperties, outDeleteProperties);

        this.inExpands = new Expands();
        this.outExpands = new Expands();
        populateInOutLists(expandProperties, expandPropertiesPattern, inExpands, outExpands);

        this.validate();
    }

    private void populateInOutLists(String properties, Pattern pattern, List<String> in, List<String> out) {
        if ((properties != null) && (properties.trim().isEmpty() == false)) {
            if (pattern.matcher(properties).matches() == false) {
                throw new RuntimeException("El parámetro properties no tiene el formato adecuado:" + properties + " , " + pattern.pattern());
            }

            String[] arrProperties = properties.replace(" ", "").split(",");
            for (String rawProperty : arrProperties) {
                String propertyName = rawProperty.replace(">", "").replace("<", "");
                if ((rawProperty.startsWith("<") == true) && (rawProperty.endsWith(">") == true)) {
                    in.add(propertyName);
                    out.add(propertyName);
                } else if ((rawProperty.startsWith("<") == true) && (rawProperty.endsWith(">") == false)) {
                    out.add(propertyName);
                } else if ((rawProperty.startsWith("<") == false) && (rawProperty.endsWith(">") == true)) {
                    in.add(propertyName);
                } else if ((rawProperty.startsWith("<") == false) && (rawProperty.endsWith(">") == false)) {
                    in.add(propertyName);
                    out.add(propertyName);
                } else {
                    throw new RuntimeException("Error de logica:" + rawProperty.startsWith("<") + " , " + rawProperty.endsWith(">"));
                }
            }

        }

    }

    public boolean isExpandInProperty(String propertyNameExpand) {
        return inExpands.isExpandProperty(propertyNameExpand);
    }

    public boolean isExpandOutProperty(String propertyNameExpand) {
        return outExpands.isExpandProperty(propertyNameExpand);
    }

    public boolean isDeleteInProperty(String propertyNameDelete) {
        for (String propertyName : inDeleteProperties) {
            if (propertyNameDelete.startsWith(propertyName)) {
                return true;
            }
        }

        return false;
    }

    public boolean isDeleteOutProperty(String propertyNameDelete) {
        for (String propertyName : outDeleteProperties) {
            if (propertyNameDelete.startsWith(propertyName)) {
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

            for (String propertyName : this.inExpands) {
                if (("*".equals(propertyName) == false) && (ReflectionUtil.existsReadPropertyInClass(entityClass, propertyName) == false)) {
                    sb.append("No existe la propiedad set de '" + propertyName + " en la clase " + entityClass.getName() + "\n");
                }
            }
            for (String propertyName : this.outExpands) {
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
