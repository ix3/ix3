/*
 * Copyright 2013 Lorenzo.
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

package es.logongas.ix3.web.controllers.metadata;

import es.logongas.ix3.persistence.services.metadata.Format;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Lorenzo
 */
public class Property {
    private Type type;
    private String className;//Solo hay valor aqui si type==OBJECT

    private boolean required;
    private boolean key;
    private boolean naturalKey;
    private Map<String,Property> properties=new LinkedHashMap<String, Property>(); //Solo hay valor aqui si type==OBJECT
    private Long minimum;
    private Long maximum ;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private Format format; //Para los Strings si es una URL o un EMAIL, etc.
    private Map<Object,String> values=new LinkedHashMap<Object,String>(); //las propiedades values o urlValues son excluyentes
    private String urlValues; //las propiedades values o urlValues son excluyentes
    private List<String> dependProperties=new ArrayList<String>(); //Solo está este valor si urlValues!=null
    
    private String label;
    private String description;    

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the key
     */
    public boolean isKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(boolean key) {
        this.key = key;
    }

    /**
     * @return the naturalKey
     */
    public boolean isNaturalKey() {
        return naturalKey;
    }

    /**
     * @param naturalKey the naturalKey to set
     */
    public void setNaturalKey(boolean naturalKey) {
        this.naturalKey = naturalKey;
    }

    /**
     * @return the properties
     */
    public Map<String,Property> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String,Property> properties) {
        this.properties = properties;
    }

    /**
     * @return the minimum
     */
    public Long getMinimum() {
        return minimum;
    }

    /**
     * @param minimum the minimum to set
     */
    public void setMinimum(Long minimum) {
        this.minimum = minimum;
    }

    /**
     * @return the maximum
     */
    public Long getMaximum() {
        return maximum;
    }

    /**
     * @param maximum the maximum to set
     */
    public void setMaximum(Long maximum) {
        this.maximum = maximum;
    }

    /**
     * @return the minLength
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * @param minLength the minLength to set
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * @return the maxLength
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return the format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * @return the values
     */
    public Map<Object,String> getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(Map<Object,String> values) {
        this.values = values;
    }

    /**
     * @return the urlValues
     */
    public String getUrlValues() {
        return urlValues;
    }

    /**
     * @param urlValues the urlValues to set
     */
    public void setUrlValues(String urlValues) {
        this.urlValues = urlValues;
    }

    /**
     * @return the dependProperties
     */
    public List<String> getDependProperties() {
        return dependProperties;
    }

    /**
     * @param dependProperties the dependProperties to set
     */
    public void setDependProperties(List<String> dependProperties) {
        this.dependProperties = dependProperties;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.getLabel();
    }
    
    
}
