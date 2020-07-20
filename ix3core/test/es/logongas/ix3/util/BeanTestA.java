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
public class BeanTestA {
    private BeanTestB prop1=new BeanTestB();

    /**
     * @return the prop1
     */
    public BeanTestB getProp1() {
        return prop1;
    }

    /**
     * @param prop1 the prop1 to set
     */
    public void setProp1(BeanTestB prop1) {
        this.prop1 = prop1;
    }
    
    public int getA() {
        return 0;
    };
    public int getA(int i) {
        return 0;
    };    
    
}
