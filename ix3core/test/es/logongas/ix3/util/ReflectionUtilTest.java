/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.util.ReflectionUtils;

/**
 *
 * @author logongas
 */
public class ReflectionUtilTest {
    
    public ReflectionUtilTest() {
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
     * Test of getAnnotation method, of class ReflectionUtil.
     */
    @Test
    public void testGetAnnotationField() throws Exception {
        System.out.println("getAnnotation en un campo");
        TestAnnotation expResult = ReflectionUtils.findField(BeanTestC.class, "prop").getAnnotation(TestAnnotation.class);
        TestAnnotation result = ReflectionUtil.getAnnotation(BeanTestC.class, "prop", TestAnnotation.class);
        assertEquals(expResult, result);
    }
    @Test
    public void testGetAnnotationMethodGet() throws Exception {
        System.out.println("getAnnotation en un metodo get");
        TestAnnotation expResult = ReflectionUtils.findMethod(BeanTestC.class, "getPropReadOnly").getAnnotation(TestAnnotation.class);
        TestAnnotation result = ReflectionUtil.getAnnotation(BeanTestC.class, "propReadOnly", TestAnnotation.class);
        assertEquals(expResult, result);
    }   
    @Test
    public void testGetAnnotationMethodSet() throws Exception {
        System.out.println("getAnnotation en un metodo set");
        TestAnnotation expResult = null;
        TestAnnotation result = ReflectionUtil.getAnnotation(BeanTestC.class, "propWriteOnly", TestAnnotation.class);
        assertEquals(expResult, result);
    }   
    @Test
    public void testGetAnnotationMethodIs() throws Exception {
        System.out.println("getAnnotation en un metodo is");
        TestAnnotation expResult = ReflectionUtils.findField(BeanTestC.class, "propBooleanReadOnly").getAnnotation(TestAnnotation.class);
        TestAnnotation result = ReflectionUtil.getAnnotation(BeanTestC.class, "propBooleanReadOnly", TestAnnotation.class);
        assertEquals(expResult, result);
    }  
    @Test
    public void testGetAnnotationPropertyNested() throws Exception {
        System.out.println("getAnnotation PropertyNested");
        TestAnnotation expResult = ReflectionUtils.findField(BeanTestC.class, "propBooleanReadOnly").getAnnotation(TestAnnotation.class);
        TestAnnotation result = ReflectionUtil.getAnnotation(BeanTestA.class, "prop1.beanTestC.propBooleanReadOnly", TestAnnotation.class);
        assertEquals(expResult, result);
    }    
    

    /**
     * Test of getField method, of class ReflectionUtil.
     */
    @Test
    public void testGetField() {
        System.out.println("getField");
        Class clazz = BeanTestC.class;
        String propertyName = "prop";
        Field expResult = ReflectionUtils.findField(BeanTestC.class, propertyName);
        Field result = ReflectionUtil.getField(clazz, propertyName);
        assertEquals(expResult, result);
    }
    @Test
    public void testGetFieldNull() {
        System.out.println("testGetFieldNull");
        Class clazz = BeanTestC.class;
        String propertyName = "nada";
        Field expResult = null;
        Field result = ReflectionUtil.getField(clazz, propertyName);
        assertEquals(expResult, result);
    }    

    /**
     * Test of getMethod method, of class ReflectionUtil.
     */
    @Test
    public void testGetMethod() {
        System.out.println("getMethod");
        Class clazz = BeanTestC.class;
        String methodName = "getProp";
        Method expResult = ReflectionUtils.findMethod(clazz, methodName);
        Method result = ReflectionUtil.getMethod(clazz, methodName);
        assertEquals(expResult, result);
    }
    @Test
    public void testGetMethodNull() {
        System.out.println("testGetMethodNull");
        Class clazz = BeanTestC.class;
        String methodName = "nada";
        Method expResult = ReflectionUtils.findMethod(clazz, methodName);
        Method result = ReflectionUtil.getMethod(clazz, methodName);
        assertEquals(expResult, result);
    }   
    
    
    @Test(expected=RuntimeException.class)
    public void testGetMethodDosMetodos() {
        System.out.println("testGetMethodDosMetodos");
        Class clazz = BeanTestA.class;
        String methodName = "getA";
        ReflectionUtil.getMethod(clazz, methodName);
    } 
    /**
     * Test of getValueFromBean method, of class ReflectionUtil.
     */
    @Test
    public void testGetValueFromBean() {
        System.out.println("getValueFromBean");
        BeanTestA obj = new BeanTestA();
        obj.getProp1().getBeanTestC().setProp(7);
        String propertyName = "prop1.beanTestC.prop";
        Object expResult = 7;
        Object result = ReflectionUtil.getValueFromBean(obj, propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of setValueToBean method, of class ReflectionUtil.
     */
    @Test
    public void testSetValueToBean() {
        System.out.println("setValueToBean");
        BeanTestA obj = new BeanTestA();
        String propertyName = "prop1.beanTestC.prop";
        Object value = 34;
        ReflectionUtil.setValueToBean(obj, propertyName, value);
        assertEquals(value,obj.getProp1().getBeanTestC().getProp());
    }
    @Test
    public void testSetValueToBeanUnaPropiedad() {
        System.out.println("testSetValueToBeanUnaPropiedad");
        BeanTestA obj = new BeanTestA();
        String propertyName = "prop1";
        Object value = new BeanTestB();
        ReflectionUtil.setValueToBean(obj, propertyName, value);
        assertEquals(value,obj.getProp1());
    }
    @Test
    public void testSetValueToBeanDosPropiedades() {
        System.out.println("testSetValueToBeanDosPropiedades");
        BeanTestA obj = new BeanTestA();
        String propertyName = "prop1.beanTestC";
        Object value = new BeanTestC();
        ReflectionUtil.setValueToBean(obj, propertyName, value);
        assertEquals(value,obj.getProp1().getBeanTestC());
    }    

    /**
     * Test of existsReadPropertyInClass method, of class ReflectionUtil.
     */
    @Test
    public void testExistsReadPropertyInClass() {
        System.out.println("existsReadPropertyInClass");
        Class clazz = BeanTestA.class;
        String propertyName = "prop1.beanTestC.propReadOnly";
        boolean expResult = true;
        boolean result = ReflectionUtil.existsReadPropertyInClass(clazz, propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of existsWritePropertyInClass method, of class ReflectionUtil.
     */
    @Test
    public void testExistsWritePropertyInClass() {
        System.out.println("existsWritePropertyInClass");
        Class clazz = BeanTestA.class;
        String propertyName = "prop1.beanTestC.propWriteOnly";
        boolean expResult = true;
        boolean result = ReflectionUtil.existsWritePropertyInClass(clazz, propertyName);
        assertEquals(expResult, result);
    }
    
    
}
