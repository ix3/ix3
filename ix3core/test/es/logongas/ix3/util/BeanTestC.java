/*
 * ix3 Copyright 2020 Lorenzo González.
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
package es.logongas.ix3.util;

/**
 *
 * @author logongas
 */
public class BeanTestC {
    
    
    private int propReadOnly;
    private int propWriteOnly;
    @TestAnnotation
    private int prop;
    private boolean propBooleanReadOnly;

    public BeanTestC() {
    }

    public BeanTestC(int propReadOnly, int propWriteOnly, int prop, boolean propBooleanReadOnly) {
        this.propReadOnly = propReadOnly;
        this.propWriteOnly = propWriteOnly;
        this.prop = prop;
        this.propBooleanReadOnly = propBooleanReadOnly;
    }



    /**
     * @return the propReadOnly
     */
    @TestAnnotation
    public int getPropReadOnly() {
        return propReadOnly;
    }

    /**
     * @param propWriteOnly the propWriteOnly to set
     */
    @TestAnnotation
    public void setPropWriteOnly(int propWriteOnly) {
        this.propWriteOnly = propWriteOnly;
    }

    /**
     * @return the prop
     */
    public int getProp() {
        return prop;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(int prop) {
        this.prop = prop;
    }

    /**
     * @return the propBooleanReadOnly
     */
    public boolean isPropBooleanReadOnly() {
        return propBooleanReadOnly;
    }
    
    
}
