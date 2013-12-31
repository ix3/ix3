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

    private boolean isRequired;
    private boolean isKey;
    private boolean isNaturalKey;
    private Map<String,Property> properties=new LinkedHashMap<String, Property>(); //Solo hay valor aqui si type==OBJECT
    private int minimum;
    private int maximum ;
    private int minLength;
    private int maxLength;
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
     * @return the isRequired
     */
    public boolean isIsRequired() {
        return isRequired;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    /**
     * @return the isKey
     */
    public boolean isIsKey() {
        return isKey;
    }

    /**
     * @param isKey the isKey to set
     */
    public void setIsKey(boolean isKey) {
        this.isKey = isKey;
    }

    /**
     * @return the isNaturalKey
     */
    public boolean isIsNaturalKey() {
        return isNaturalKey;
    }

    /**
     * @param isNaturalKey the isNaturalKey to set
     */
    public void setIsNaturalKey(boolean isNaturalKey) {
        this.isNaturalKey = isNaturalKey;
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
    public int getMinimum() {
        return minimum;
    }

    /**
     * @param minimum the minimum to set
     */
    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    /**
     * @return the maximum
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * @param maximum the maximum to set
     */
    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    /**
     * @return the minLength
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * @param minLength the minLength to set
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @return the maxLength
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(int maxLength) {
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
}
