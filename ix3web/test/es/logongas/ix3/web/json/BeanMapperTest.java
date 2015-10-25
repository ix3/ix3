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
package es.logongas.ix3.web.json;

import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.core.BusinessMessage;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author logongas
 */
public class BeanMapperTest {
    
    public BeanMapperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isExpandInProperty method, of class BeanMapper.
     */
    @Test
    public void testIsExpandInProperty1() {
        System.out.println("isExpandInProperty 1");
        String propertyNameExpand = "prop1";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"prop1,prop2.beanTestC",null,null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }

    
    @Test
    public void testIsExpandInProperty2() {
        System.out.println("isExpandInProperty 2");
        String propertyNameExpand = "prop1";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"prop2.beanTestC,prop1",null,null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    } 
    
    @Test
    public void testIsExpandInProperty3() {
        System.out.println("isExpandInProperty 3");
        String propertyNameExpand = "prop2.beanTestC";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"prop2.beanTestC,prop1",null,null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }  
    
    @Test
    public void testIsExpandInProperty4() {
        System.out.println("isExpandInProperty 4");
        String propertyNameExpand = "prop2.beanTestC";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"prop1,prop2.beanTestC",null,null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }     
    
    @Test
    public void testIsExpandInProperty5() {
        System.out.println("isExpandInProperty 5");
        String propertyNameExpand = "ninguna";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"prop1,prop2.beanTestC",null,null);
        boolean expResult = false;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }    
    @Test
    public void testIsExpandInProperty6() {
        System.out.println("isExpandInProperty 6");
        String propertyNameExpand = "ninguna";
        BeanMapper instance = new BeanMapper(BeanTestA.class);
        boolean expResult = false;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    } 
    @Test
    public void testIsExpandInProperty7() {
        System.out.println("isExpandInProperty 7");
        String propertyNameExpand = "prop2";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,"prop1,prop2.beanTestC",null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    } 
    @Test
    public void testIsExpandInProperty8() {
        System.out.println("isExpandInProperty 8");
        String propertyNameExpand = "cualquiera.propiedad";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,"prop1,prop2.beanTestC,*",null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }    
    @Test
    public void testIsExpandInProperty9() {
        System.out.println("isExpandInProperty 9");
        String propertyNameExpand = "cualquiera.propiedad";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,"*",null);
        boolean expResult = true;
        boolean result = instance.isExpandInProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }
    @Test(expected = RuntimeException.class)
    public void testIsExpandInProperty10() {
        System.out.println("isExpandInProperty 10");
        String propertyNameExpand = "cualquiera.propiedad";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,"noexistrepropeidad",null);

    }    
    
    /**
     * Test of isExpandOutProperty method, of class BeanMapper.
     */
    @Test
    public void testIsExpandOutProperty1() {
        System.out.println("isExpandOutProperty 1");
        String propertyNameExpand = "prop2";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"prop1,prop2.beanTestC",null,null);
        boolean expResult = true;
        boolean result = instance.isExpandOutProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }
    @Test
    public void testIsExpandOutProperty2() {
        System.out.println("isExpandOutProperty 2");
        String propertyNameExpand = "prop2.beanTestC";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,null,"prop1,prop2.beanTestC");
        boolean expResult = true;
        boolean result = instance.isExpandOutProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }
    @Test
    public void testIsExpandOutProperty3() {
        System.out.println("isExpandOutProperty 3");
        String propertyNameExpand = "cualquier.propiedad";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,null,"*");
        boolean expResult = true;
        boolean result = instance.isExpandOutProperty(propertyNameExpand);
        assertEquals(expResult, result);
    }
   
    
    /**
     * Test of isDeleteInProperty method, of class BeanMapper.
     */
    @Test
    public void testIsDeleteInProperty1() {
        System.out.println("isDeleteInProperty 1");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1,prop2.beanTestC",null,null,null,null,null);
        boolean expResult = true;
        boolean result = instance.isDeleteInProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }
    @Test
    public void testIsDeleteInProperty2() {
        System.out.println("isDeleteInProperty 2");
        String propertyNameAllow = "prop2.beanTestC.prop";
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1,prop2.beanTestC.prop",null,null,null,null,null);
        boolean expResult = true;
        boolean result = instance.isDeleteInProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }
    @Test
    public void testIsDeleteInProperty3() {
        System.out.println("isDeleteInProperty 3");
        String propertyNameAllow = "prop2";
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1,prop2.beanTestC.prop",null,null,null,null,null);
        boolean expResult = false;
        boolean result = instance.isDeleteInProperty(propertyNameAllow);
        assertEquals(expResult, result);
    } 
    @Test(expected = RuntimeException.class)
    public void testIsDeleteInProperty4() {
        System.out.println("isDeleteInProperty 4");
        String propertyNameAllow = "cualquier.propeidad";
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1,prop2.beanTestC.prop,*",null,null,null,null,null);
        boolean expResult = false;
        boolean result = instance.isDeleteInProperty(propertyNameAllow);
        assertEquals(expResult, result);
    } 
    @Test
    public void testIsDeleteInProperty5() {
        System.out.println("isDeleteInProperty 5");
        String propertyNameAllow = "prop2.beanTestC.prop";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,"prop1,prop2.beanTestC.prop",null,null,null,null);
        boolean expResult = true;
        boolean result = instance.isDeleteInProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }
    @Test(expected = RuntimeException.class)
    public void testIsDeleteInProperty6() {
        System.out.println("isDeleteInProperty 6");
        String propertyNameAllow = "cualquier.propiedad";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,"prop1,prop2.beanTestC.prop",null,null,null,null);
        boolean expResult = false;
        boolean result = instance.isDeleteInProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }
    /**
     * Test of isDeleteOutProperty method, of class BeanMapper.
     */
    @Test
    public void testIsDeleteOutProperty1() {
        System.out.println("isDeleteOutProperty 1");
        String propertyNameAllow = "prop2.beanTestC.prop";
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1,prop2.beanTestC.prop",null,null,null,null,null);
        boolean expResult = true;
        boolean result = instance.isDeleteOutProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }
    @Test
    public void testIsDeleteOutProperty2() {
        System.out.println("isDeleteOutProperty 2");
        String propertyNameAllow = "prop2.beanTestC.prop";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,"prop1,prop2.beanTestC.prop",null,null,null);
        boolean expResult = true;
        boolean result = instance.isDeleteOutProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }
    @Test
    public void testIsDeleteOutProperty3() {
        System.out.println("isDeleteOutProperty 2");
        String propertyNameAllow = "prop2.beanTestC.privateProperty";
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,null,null,null);
        boolean expResult = true;
        boolean result = instance.isDeleteOutProperty(propertyNameAllow);
        assertEquals(expResult, result);
    }    
    
    /**
     * Test of getEntityClass method, of class BeanMapper.
     */    
    @Test
    public void testGetEntityClass() {
        System.out.println("getEntityClass");
        BeanMapper instance = new BeanMapper(this.getClass(),null,null,null,null,null,null);
        Class expResult = this.getClass();
        Class result = instance.getEntityClass();
        assertEquals(expResult, result);
    }
    


    @Test
    public void testValidate2() {
        System.out.println("validate2");
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1","prop1","prop1","prop1","prop1","prop1");
    }
    @Test
    public void testValidate3() {
        System.out.println("validate2");
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1.beanTestC","prop1.beanTestC","prop1.beanTestC","prop1.beanTestC","prop1.beanTestC","prop1.beanTestC");
    }
    @Test(expected = RuntimeException.class)
    public void testValidate4() {
        System.out.println("validate4");
        BeanMapper instance = new BeanMapper(BeanTestA.class,"prop1.beanTestC,propKKKKK","prop1.beanTestC","prop1.beanTestC","prop1.beanTestC","prop1.beanTestC","prop1.beanTestC");
    } 
    @Test
    public void testValidate5() {
        System.out.println("validate5");
        BeanMapper instance = new BeanMapper(BeanTestA.class,null,null,null,"*","*","*");
    }     
    
}
