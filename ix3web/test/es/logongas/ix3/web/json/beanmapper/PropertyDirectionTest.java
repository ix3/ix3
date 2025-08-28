/*
 * Copyright 2025 logongas.
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author logongas
 */
public class PropertyDirectionTest {
    
    public PropertyDirectionTest() {
    }



    @Test
    public void testIsInToServer() {
        System.out.println("isInToServer");
        PropertyDirection instance = PropertyDirection.IN_TO_SERVER;
        boolean expResult = true;
        boolean result = instance.isInToServer();
        assertEquals(expResult, result);
    }
    @Test
    public void testIsInToServer2() {
        System.out.println("isInToServer2");
        PropertyDirection instance = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        boolean expResult = true;
        boolean result = instance.isInToServer();
        assertEquals(expResult, result);
    }
    @Test
    public void testIsInToServer3() {
        System.out.println("isInToServer3");
        PropertyDirection instance = PropertyDirection.OUT_FROM_SERVER;
        boolean expResult = false;
        boolean result = instance.isInToServer();
        assertEquals(expResult, result);
    }    
    @Test
    public void testIsInToServer4() {
        System.out.println("isInToServer4");
        PropertyDirection instance = PropertyDirection.NONE;
        boolean expResult = false;
        boolean result = instance.isInToServer();
        assertEquals(expResult, result);
    }    
    
    @Test
    public void testIsOutFromServer() {
        System.out.println("isOutFromServer");
        PropertyDirection instance = PropertyDirection.OUT_FROM_SERVER;
        boolean expResult = true;
        boolean result = instance.isOutFromServer();
        assertEquals(expResult, result);
    }
    @Test
    public void testIsOutFromServer2() {
        System.out.println("isOutFromServer2");
        PropertyDirection instance = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        boolean expResult = true;
        boolean result = instance.isOutFromServer();
        assertEquals(expResult, result);
    }
    @Test
    public void testIsOutFromServer3() {
        System.out.println("isOutFromServer3");
        PropertyDirection instance = PropertyDirection.IN_TO_SERVER;
        boolean expResult = false;
        boolean result = instance.isOutFromServer();
        assertEquals(expResult, result);
    }
    @Test
    public void testIsOutFromServer4() {
        System.out.println("isOutFromServer4");
        PropertyDirection instance = PropertyDirection.NONE;
        boolean expResult = false;
        boolean result = instance.isOutFromServer();
        assertEquals(expResult, result);
    }    
    
    
    @Test
    public void testNoneJoin1() {
        System.out.println("testNoneJoin1");
        PropertyDirection propertyDirection = PropertyDirection.NONE;
        PropertyDirection propertyDirectionJoin = PropertyDirection.NONE;
        PropertyDirection expResult = PropertyDirection.NONE;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }    
    @Test
    public void testNoneJoin2() {
        System.out.println("testNoneJoin2");
        PropertyDirection propertyDirection = PropertyDirection.NONE;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }         
    @Test
    public void testNoneJoin3() {
        System.out.println("testNoneJoin3");
        PropertyDirection propertyDirection = PropertyDirection.NONE;
        PropertyDirection propertyDirectionJoin = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }   
    @Test
    public void testNoneJoin4() {
        System.out.println("testNoneJoin4");
        PropertyDirection propertyDirection = PropertyDirection.NONE;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }    
    
    
    
    
    @Test
    public void testInToServerJoin1() {
        System.out.println("testInToServerJoin1");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.NONE;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }    
    @Test
    public void testInToServerJoin2() {
        System.out.println("testInToServerJoin2");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }         
    @Test
    public void testInToServerJoin3() {
        System.out.println("testInToServerJoin3");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }   
    @Test
    public void testInToServerJoin4() {
        System.out.println("testInToServerJoin4");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }    
    
    
    
    
    @Test
    public void testOutToServerJoin1() {
        System.out.println("testOutToServerJoin1");
        PropertyDirection propertyDirection = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.NONE;
        PropertyDirection expResult = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }    
    @Test
    public void testOutToServerJoin2() {
        System.out.println("testOutToServerJoin2");
        PropertyDirection propertyDirection = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }         
    @Test
    public void testOutToServerJoin3() {
        System.out.println("testOutToServerJoin3");
        PropertyDirection propertyDirection = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }   
    @Test
    public void testOutToServerJoin4() {
        System.out.println("testOutToServerJoin4");
        PropertyDirection propertyDirection = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }        
    
    
    
    @Test
    public void testInOutToServerJoin1() {
        System.out.println("testInOutToServerJoin1");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.NONE;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }    
    @Test
    public void testInOutToServerJoin2() {
        System.out.println("testInOutToServerJoin2");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }         
    @Test
    public void testInOutToServerJoin3() {
        System.out.println("testInOutToServerJoin3");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }   
    @Test
    public void testInOutToServerJoin4() {
        System.out.println("testInOutToServerJoin4");
        PropertyDirection propertyDirection = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection propertyDirectionJoin = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection expResult = PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        PropertyDirection result = propertyDirection.join(propertyDirectionJoin);
        assertEquals(expResult, result);
    }        
    
}
