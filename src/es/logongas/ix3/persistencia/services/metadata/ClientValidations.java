/*
 * Copyright 2012 Lorenzo González.
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
package es.logongas.ix3.persistencia.services.metadata;

import java.util.regex.Pattern;

/**
 * Diversas validaciones a realizar en la parte cliente.
 * @author Lorenzo González
 */
public class ClientValidations {
    private boolean readOnlyForInsert;
    private boolean readOnlyForUpdate;
    private boolean required;    
    private Integer maxLength ;    
    private Integer max ;    
    private Integer min ;    
    private Pattern pattern;    

    public ClientValidations(boolean readOnlyForInsert, boolean readOnlyForUpdate, boolean required, int maxLength, int max, int min, Pattern pattern) {
        this.readOnlyForInsert = readOnlyForInsert;
        this.readOnlyForUpdate = readOnlyForUpdate;
        this.required = required;
        this.maxLength = maxLength;
        this.max = max;
        this.min = min;
        this.pattern = pattern;
    }

    public ClientValidations() {
        this.readOnlyForInsert = false;
        this.readOnlyForUpdate = false;
        this.required = false;
        this.maxLength = null;
        this.max = null;
        this.min = null;
        this.pattern = pattern;
    }

    
    
    /**
     * @return the readOnlyForInsert
     */
    public boolean isReadOnlyForInsert() {
        return readOnlyForInsert;
    }

    /**
     * @return the readOnlyForUpdate
     */
    public boolean isReadOnlyForUpdate() {
        return readOnlyForUpdate;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return the maxLength
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * @return the max
     */
    public Integer getMax() {
        return max;
    }

    /**
     * @return the min
     */
    public Integer getMin() {
        return min;
    }

    /**
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * @param readOnlyForInsert the readOnlyForInsert to set
     */
    public void setReadOnlyForInsert(boolean readOnlyForInsert) {
        this.readOnlyForInsert = readOnlyForInsert;
    }

    /**
     * @param readOnlyForUpdate the readOnlyForUpdate to set
     */
    public void setReadOnlyForUpdate(boolean readOnlyForUpdate) {
        this.readOnlyForUpdate = readOnlyForUpdate;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param max the max to set
     */
    public void setMax(Integer max) {
        this.max = max;
    }

    /**
     * @param min the min to set
     */
    public void setMin(Integer min) {
        this.min = min;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
