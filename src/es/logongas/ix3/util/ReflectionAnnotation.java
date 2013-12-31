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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Utilidades para obtener anotaciones de clases
 * @author Lorenzo
 */
public class ReflectionAnnotation {
    
    /**
     * Obtiene una anotación de una clase. La anotación se obtiene de la propiedad o del método
     * @param <T>
     * @param baseClass La clase en la que se busca la anotación
     * @param propertyName El nombre de la propiedad sobre la que se busca la anotación
     * @param annotationClass El tipo de anotación a buscar
     * @return Retorna la anotación.Si la anotación existe tanto en la propiedad como en el método se retorna solo el de la propiedad.
     */
    static public <T extends Annotation> T getAnnotation(Class baseClass,String propertyName,Class<T> annotationClass) {      
        if (annotationClass==null) {
            throw new IllegalArgumentException("El argumento annotationClass no puede ser null");
        }
        
        if (baseClass==null) {
            return null;
        }
        
        if ((propertyName == null) || (propertyName.trim().equals(""))) {
            throw new IllegalArgumentException("El argumento propertyName no puede ser null");
        }
        

        T annotationField = getFieldAnnotation(baseClass, propertyName,annotationClass);
        if (annotationField != null) {
            return annotationField;
        }

        T annotationMethod = getMethodAnnotation(baseClass, propertyName,annotationClass);
        if (annotationMethod != null) {
            return annotationMethod;
        }

        //No hemos encontrado la anotación
        return null;
        
    }

    static private <T extends Annotation> T getFieldAnnotation(Class baseClass,String propertyName,Class<T> annotationClass) {
        Field field = ReflectionUtils.findField(baseClass, propertyName);
        if (field == null) {
            return null;
        }

        T annotation=field.getAnnotation(annotationClass);
        
        return annotation;
    }    
    
    static private <T extends Annotation> T getMethodAnnotation(Class baseClass,String propertyName,Class<T> annotationClass) {
        String suffixMethodName = StringUtils.capitalize(propertyName);
        Method method = ReflectionUtils.findMethod(baseClass, "get" + suffixMethodName);
        if (method == null) {
            method = ReflectionUtils.findMethod(baseClass, "is" + suffixMethodName);
            if (method == null) {
                return null;
            }
        }

        T annotation=method.getAnnotation(annotationClass);
        
        return  annotation;
    }
    
    
}
