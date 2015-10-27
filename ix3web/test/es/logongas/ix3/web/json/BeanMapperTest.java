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

    @Test
    public void testIsDeleteProperty1() {
        System.out.println("isDeleteProperty 1");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1,prop2.beanTestC", null);
        assertEquals(true, beanMapper.isDeleteInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isDeleteOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsDeleteProperty2() {
        System.out.println("isDeleteProperty 2");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1,<prop2.beanTestC>", null);
        assertEquals(true, beanMapper.isDeleteInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isDeleteOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsDeleteProperty3() {
        System.out.println("isDeleteInProperty 3");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1,prop2.beanTestC>", null);
        assertEquals(true, beanMapper.isDeleteInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isDeleteOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsDeleteProperty4() {
        System.out.println("isDeleteProperty 4");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1,<prop2.beanTestC", null);
        assertEquals(false, beanMapper.isDeleteInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isDeleteOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsDeleteProperty5() {
        System.out.println("isDeleteProperty 5");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1", null);
        assertEquals(false, beanMapper.isDeleteInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isDeleteOutProperty(propertyNameAllow));
    }
    
    @Test
    public void testIsDeleteProperty6() {
        System.out.println("isDeleteProperty 6");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, null);
        assertEquals(false, beanMapper.isDeleteInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isDeleteOutProperty(propertyNameAllow));
    }    
    
    

    public void testIsExpandProperty1() {
        System.out.println("isExpandProperty 1");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1,prop2.beanTestC", null);
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty2() {
        System.out.println("isExpandProperty 2");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,<prop2.beanTestC>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty3() {
        System.out.println("isExpandInProperty 3");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,prop2.beanTestC>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty4() {
        System.out.println("isExpandProperty 4");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,<prop2.beanTestC");
        assertEquals(false, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    public void testIsExpandProperty5() {
        System.out.println("isExpandProperty 5");
        String propertyNameAllow = "prop2";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, "prop1,prop2.beanTestC", null);
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty6() {
        System.out.println("isExpandProperty 6");
        String propertyNameAllow = "prop2";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,<prop2.beanTestC>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty7() {
        System.out.println("isExpandInProperty 7");
        String propertyNameAllow = "prop2";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,prop2.beanTestC>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty8() {
        System.out.println("isExpandProperty 8");
        String propertyNameAllow = "prop2";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,<prop2.beanTestC");
        assertEquals(false, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }    
    
    
    @Test
    public void testIsExpandProperty9() {
        System.out.println("isExpandProperty 9");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1");
        assertEquals(false, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isExpandOutProperty(propertyNameAllow));
    }

    @Test
    public void testIsExpandProperty10() {
        System.out.println("isExpandProperty 10");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, null);
        assertEquals(false, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isExpandOutProperty(propertyNameAllow));
    }  
    
    @Test
    public void testIsExpandProperty11() {
        System.out.println("isExpandProperty 11");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "*");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }    
    
    @Test
    public void testIsExpandProperty12() {
        System.out.println("isExpandProperty 12");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "<*");
        assertEquals(false, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }
    
    @Test
    public void testIsExpandProperty13() {
        System.out.println("isExpandProperty 13");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "*>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isExpandOutProperty(propertyNameAllow));
    }
    
    @Test
    public void testIsExpandProperty14() {
        System.out.println("isExpandProperty 14");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "<*>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }    
   
    @Test
    public void testIsExpandProperty15() {
        System.out.println("isExpandProperty 15");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,*");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }    
    
    @Test
    public void testIsExpandProperty16() {
        System.out.println("isExpandProperty 16");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,<*");
        assertEquals(false, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }
    
    @Test
    public void testIsExpandProperty17() {
        System.out.println("isExpandProperty 17");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,*>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(false, beanMapper.isExpandOutProperty(propertyNameAllow));
    }
    
    @Test
    public void testIsExpandProperty18() {
        System.out.println("isExpandProperty 18");
        String propertyNameAllow = "prop2.beanTestC";
        BeanMapper beanMapper = new BeanMapper(BeanTestA.class, null, "prop1,<*>");
        assertEquals(true, beanMapper.isExpandInProperty(propertyNameAllow));
        assertEquals(true, beanMapper.isExpandOutProperty(propertyNameAllow));
    }     
    
    /**
     * Test of getEntityClass method, of class BeanMapper.
     */
    @Test
    public void testGetEntityClass() {
        System.out.println("getEntityClass");
        BeanMapper instance = new BeanMapper(this.getClass());
        Class expResult = this.getClass();
        Class result = instance.getEntityClass();
        assertEquals(expResult, result);
    }

    @Test
    public void testValidate2() {
        System.out.println("validate2");
        BeanMapper instance = new BeanMapper(BeanTestA.class, "prop1", "prop1");
    }

    @Test
    public void testValidate3() {
        System.out.println("validate2");
        BeanMapper instance = new BeanMapper(BeanTestA.class, "prop1.beanTestC", "prop1.beanTestC");
    }

    @Test(expected = RuntimeException.class)
    public void testValidate4() {
        System.out.println("validate4");
        BeanMapper instance = new BeanMapper(BeanTestA.class, "prop1.beanTestC,propKKKKK", "prop1.beanTestC");
    }

    @Test(expected = RuntimeException.class)
    public void testValidate5() {
        System.out.println("validate5");
        BeanMapper instance = new BeanMapper(BeanTestA.class, "prop1.beanTestC", "prop1.beanTestC,propKKKKK");
    }

    @Test
    public void testValidate6() {
        System.out.println("validate6");
        BeanMapper instance = new BeanMapper(BeanTestA.class, null, "*");
    }

}
