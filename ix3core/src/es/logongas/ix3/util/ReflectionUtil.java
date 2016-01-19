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
package es.logongas.ix3.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Utilidades para obtener anotaciones de clases
 *
 * @author Lorenzo
 */
public class ReflectionUtil {

    /**
     * Obtiene una anotación de una clase. La anotación se obtiene de la
     * propiedad o del método
     *
     * @param <T>
     * @param baseClass La clase en la que se busca la anotación
     * @param propertyName El nombre de la propiedad sobre la que se busca la
     * anotación
     * @param annotationClass El tipo de anotación a buscar
     * @return Retorna la anotación.Si la anotación existe tanto en la propiedad
     * como en el método se retorna solo el de la propiedad.
     */
    static public <T extends Annotation> T getAnnotation(Class baseClass, String propertyName, Class<T> annotationClass) {
        if (annotationClass == null) {
            throw new IllegalArgumentException("El argumento annotationClass no puede ser null");
        }

        if (baseClass == null) {
            return null;
        }

        if ((propertyName == null) || (propertyName.trim().equals(""))) {
            throw new IllegalArgumentException("El argumento propertyName no puede ser null");
        }

        String leftPropertyName; //El nombre de la propiedad antes del primer punto
        String rigthPropertyName; //El nombre de la propiedad antes del primer punto

        int indexPoint = propertyName.indexOf(".");
        if (indexPoint < 0) {
            leftPropertyName = propertyName;
            rigthPropertyName = null;
        } else if ((indexPoint > 0) && (indexPoint < (propertyName.length() - 1))) {
            leftPropertyName = propertyName.substring(0, indexPoint);
            rigthPropertyName = propertyName.substring(indexPoint + 1);
        } else {
            throw new RuntimeException("El punto no puede estar ni al principio ni al final");
        }        

        if (rigthPropertyName != null) {
            Field leftField=getField(baseClass, leftPropertyName);
            if (leftField==null) {
                throw new RuntimeException("No existe el campo " + leftPropertyName + " en la clase " + baseClass.getName());
            }
            return getAnnotation(leftField.getType(), rigthPropertyName, annotationClass);
        } else {
            T annotationField = getFieldAnnotation(baseClass, leftPropertyName, annotationClass);
            if (annotationField != null) {
                return annotationField;
            }

            T annotationMethod = getMethodAnnotation(baseClass, leftPropertyName, annotationClass);
            if (annotationMethod != null) {
                return annotationMethod;
            }

            //No hemos encontrado la anotación
            return null;
        }            

    }

    static private <T extends Annotation> T getFieldAnnotation(Class baseClass, String propertyName, Class<T> annotationClass) {
        Field field = getField(baseClass, propertyName);
        if (field == null) {

            return null;
        }

        T annotation = field.getAnnotation(annotationClass);

        return annotation;
    }

    static private <T extends Annotation> T getMethodAnnotation(Class baseClass, String methodName, Class<T> annotationClass) {
        String suffixMethodName = StringUtils.capitalize(methodName);
        Method method = getMethod(baseClass, "get" + suffixMethodName);
        if (method == null) {
            method = getMethod(baseClass, "is" + suffixMethodName);
            if (method == null) {
                method = getMethod(baseClass, methodName);
                if (method == null) {

                    return null;
                }
            }
        }
        T annotation = method.getAnnotation(annotationClass);

        return annotation;
    }

    static public Field getField(Class clazz, String propertyName) {
        return ReflectionUtils.findField(clazz, propertyName);
    }

    static public Method getMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getMethods();

        return findUniqueMethodByName(methods, methodName);
    }
    static public Method getDeclaredMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();

        return findUniqueMethodByName(methods, methodName);
    }    
    
    
    
    public static boolean isFieldParametrizedList(Field field,Class listClass) {
        Type type=field.getGenericType();
        
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType=(ParameterizedType)type;
            if (parameterizedType.getRawType().equals(List.class)) {
                Type[] actualTypeArguments=parameterizedType.getActualTypeArguments();
                
                if ((actualTypeArguments==null) || (actualTypeArguments.length!=1)) {
                    return false;
                } else {
                    if (actualTypeArguments[0].equals(listClass)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                
            } else {
                return false;
            }
            
        } else {
            return false;
        }
    }
    public static boolean isFieldParametrizedMap(Field field,Class keyClass,Class valueClass) {
        Type type=field.getGenericType();
        
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType=(ParameterizedType)type;
            if (parameterizedType.getRawType().equals(Map.class)) {
                Type[] actualTypeArguments=parameterizedType.getActualTypeArguments();
                
                if ((actualTypeArguments==null) || (actualTypeArguments.length!=2)) {
                    return false;
                } else {
                    if ((actualTypeArguments[0].equals(keyClass)) && (actualTypeArguments[1].equals(valueClass))) {
                        return true;
                    } else {
                        return false;
                    }
                }
                
            } else {
                return false;
            }
            
        } else {
            return false;
        }
    }
    
    static private Method findUniqueMethodByName(Method[] methods, String methodName) {
        Method method = null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {

                if (method != null) {
                    throw new RuntimeException("Existen dos o mas metodos llamados '" + methodName);
                }

                method = methods[i];
            }
        }

        return method;

    }    

    /**
     * Obtiene el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param propertyName El nombre de la propiedad. Se permiten
     * "subpropiedades" separadas por "."
     * @return El valor de la propiedad
     */
    static public Object getValueFromBean(Object obj, String propertyName) {
        try {
            Object value;

            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            if ((propertyName == null) || (propertyName.trim().isEmpty())) {
                throw new RuntimeException("El parametro propertyName no puede ser null o estar vacio");
            }

            String leftPropertyName; //El nombre de la propiedad antes del primer punto
            String rigthPropertyName; //El nombre de la propiedad antes del primer punto

            int indexPoint = propertyName.indexOf(".");
            if (indexPoint < 0) {
                leftPropertyName = propertyName;
                rigthPropertyName = null;
            } else if ((indexPoint > 0) && (indexPoint < (propertyName.length() - 1))) {
                leftPropertyName = propertyName.substring(0, indexPoint);
                rigthPropertyName = propertyName.substring(indexPoint + 1);
            } else {
                throw new RuntimeException("El punto no puede estar ni al principio ni al final");
            }

            Method readMethod = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(leftPropertyName)) {
                    readMethod = propertyDescriptor.getReadMethod();
                }
            }

            if (readMethod == null) {
                throw new RuntimeException("No existe la propiedad:" + leftPropertyName);
            }

            if (rigthPropertyName != null) {
                Object valueProperty = readMethod.invoke(obj);
                value = getValueFromBean(valueProperty, rigthPropertyName);
            } else {
                value = readMethod.invoke(obj);
            }

            return value;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Establecer el valor en un bena
     *
     * @param obj El objeto al que se establece el valor
     * @param propertyName El nombre de la propieda a establecer el valor. Se
     * permiten "subpropiedades" separadas por "."
     * @param value El valor a establecer.
     */
    static public void setValueToBean(Object obj, String propertyName, Object value) {
        try {
            if ((propertyName == null) || (propertyName.trim().isEmpty())) {
                throw new RuntimeException("El parametro propertyName no puede ser null o estar vacio");
            }
            if (obj==null) {
                throw new RuntimeException("El parametro obj no puede ser null");
            }
            
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();            
            
            String leftPropertyName; //El nombre de la propiedad antes del primer punto
            String rigthPropertyName; //El nombre de la propiedad antes del primer punto

            int indexPoint = propertyName.indexOf(".");
            if (indexPoint < 0) {
                leftPropertyName = propertyName;
                rigthPropertyName = null;
            } else if ((indexPoint > 0) && (indexPoint < (propertyName.length() - 1))) {
                leftPropertyName = propertyName.substring(0, indexPoint);
                rigthPropertyName = propertyName.substring(indexPoint + 1);
            } else {
                throw new RuntimeException("El punto no puede estar ni al principio ni al final");
            }

            Method readMethod = null;
            Method writeMethod = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(leftPropertyName)) {
                    readMethod = propertyDescriptor.getReadMethod();
                    writeMethod = propertyDescriptor.getWriteMethod();
                }
            }

            if (rigthPropertyName != null) {
                if (readMethod == null) {
                    throw new RuntimeException("No existe la propiedad de lectura:" + leftPropertyName);
                }
                Object valueProperty = readMethod.invoke(obj);
                setValueToBean(valueProperty, rigthPropertyName, value);
            } else {
                if (writeMethod == null) {
                    throw new RuntimeException("No existe la propiedad de escritura:" + leftPropertyName);
                }
                writeMethod.invoke(obj, new Object[]{value});
            }

        } catch (Exception ex) {
            throw new RuntimeException("obj:" + obj + " propertyName=" + propertyName + " value=" + value,ex);
        }
    }

    /**
     *
     * @param clazz
     * @param propertyName El nombre de la propiedad permite que sean varias
     * "nested" con puntos. Ej: "prop1.prop2.prop3"
     * @return
     */
    static public boolean existsReadPropertyInClass(Class clazz, String propertyName) {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor(clazz, propertyName);

        if (propertyDescriptor.getReadMethod() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param clazz
     * @param propertyName El nombre de la propiedad permite que sean varias
     * "nested" con puntos. Ej: "prop1.prop2.prop3"
     * @return
     */
    static public boolean existsWritePropertyInClass(Class clazz, String propertyName) {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor(clazz, propertyName);

        if (propertyDescriptor.getWriteMethod() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Este método
     *
     * @param clazz
     * @param propertyName El nombre de la propiedad permite que sean varias
     * "nested" con puntos. Ej: "prop1.prop2.prop3"
     * @return
     */
    private static PropertyDescriptor getPropertyDescriptor(Class clazz, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if ((propertyName == null) || (propertyName.trim().isEmpty())) {
                throw new RuntimeException("El parametro propertyName no puede ser null o estar vacio");
            }

            String leftPropertyName; //El nombre de la propiedad antes del primer punto
            String rigthPropertyName; //El nombre de la propiedad despues del primer punto

            int indexPoint = propertyName.indexOf(".");
            if (indexPoint < 0) {
                leftPropertyName = propertyName;
                rigthPropertyName = null;
            } else if ((indexPoint > 0) && (indexPoint < (propertyName.length() - 1))) {
                leftPropertyName = propertyName.substring(0, indexPoint);
                rigthPropertyName = propertyName.substring(indexPoint + 1);
            } else {
                throw new RuntimeException("El punto no puede estar ni al principio ni al final");
            }
            
            PropertyDescriptor propertyDescriptorFind = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(leftPropertyName)) {
                    propertyDescriptorFind = propertyDescriptor;
                    break;
                }
            }

            if (propertyDescriptorFind == null) {
                throw new RuntimeException("No existe el propertyDescriptorFind de " + leftPropertyName);
            }

            if (rigthPropertyName != null) {
                Method readMethod = propertyDescriptorFind.getReadMethod();
                if (readMethod == null) {
                    throw new RuntimeException("No existe el metodo 'get' de " + leftPropertyName);
                }

                Class readClass = readMethod.getReturnType();
                return getPropertyDescriptor(readClass, rigthPropertyName);
            } else {
                return propertyDescriptorFind;
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
