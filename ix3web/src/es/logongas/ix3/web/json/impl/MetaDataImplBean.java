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
package es.logongas.ix3.web.json.impl;

import es.logongas.ix3.dao.metadata.CollectionType;
import es.logongas.ix3.dao.metadata.Constraints;
import es.logongas.ix3.dao.metadata.Format;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.metadata.MetaType;
import es.logongas.ix3.dao.metadata.ValuesList;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Lorenzo González
 */
public class MetaDataImplBean implements MetaData {

    private final Class clazz;
    private final boolean read;
    private final boolean write;
    private final CollectionType collectionType;
    private final MetaDataFactory metaDataFactory;
    private final String propertyName;
    private final String propertyPath;

    public MetaDataImplBean(Class clazz, CollectionType collectionType, boolean read, boolean write, MetaDataFactory metaDataFactory, String propertyName,String propertyPath) {
        this.clazz = clazz;
        this.collectionType = collectionType;
        this.read = read;
        this.write = write;
        this.metaDataFactory = metaDataFactory;
        this.propertyName = propertyName;
        this.propertyPath=propertyPath;
    }

    @Override
    public Class getType() {
        return clazz;
    }

    @Override
    public MetaType getMetaType() {
        if (clazz.isPrimitive()) {
            return MetaType.Scalar;
        } else if ((clazz == Object.class) || (clazz == Byte.class) || (clazz == Short.class) || (clazz == Integer.class) || (clazz == Long.class) || (clazz == Float.class) || (clazz == Double.class) || (clazz == Boolean.class) || (clazz == Character.class) || (clazz == BigDecimal.class) || (clazz == BigInteger.class) || (clazz == Date.class) || (clazz == String.class)) {
            return MetaType.Scalar;
        } else if (clazz.isEnum() == true) {
            return MetaType.Scalar;
        } else if (metaDataFactory.getMetaData(clazz)!=null) {
            return MetaType.Entity;
        } else {
            return MetaType.Component;
        }
    }

    @Override
    public Map<String, MetaData> getPropertiesMetaData() {
        try {
            Map<String, MetaData> propertiesMetaData = new HashMap<String, MetaData>();

            //Si es un escalar seguro que no hay propieades. Así que mejor no intentar guscarlas pq seguro que hay algún "get".
            if (getMetaType() == MetaType.Scalar) {
                return propertiesMetaData;
            }

            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyName = propertyDescriptor.getName();
                Class propertyClass = propertyDescriptor.getPropertyType();

                MetaData metaData = metaDataFactory.getMetaData(propertyClass);
                if (metaData == null) {
                    CollectionType newCollectionType;
                    Class realPropertyClass;

                    if (Set.class.isAssignableFrom(propertyClass)) {
                        newCollectionType = CollectionType.Set;
                        realPropertyClass = getCollectionClass(propertyDescriptor.getReadMethod());
                        metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory, propertyName,propertyPath+"."+propertyName);
                    } else if (List.class.isAssignableFrom(propertyClass)) {
                        newCollectionType = CollectionType.List;
                        realPropertyClass = getCollectionClass(propertyDescriptor.getReadMethod());
                        metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory, propertyName,propertyPath+"."+propertyName);
                    } else if (Map.class.isAssignableFrom(propertyClass)) {
                        newCollectionType = CollectionType.Map;
                        realPropertyClass = getCollectionClass(propertyDescriptor.getReadMethod());
                        metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory, propertyName,propertyPath+"."+propertyName);
                    } else {
                        //No es una colección
                        newCollectionType = null;
                        realPropertyClass = propertyClass;

                        metaData = metaDataFactory.getMetaData(realPropertyClass);
                        if (metaData == null) {
                            metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory, propertyName,propertyPath+"."+propertyName);
                        }

                    }

                }

                boolean add = true;
                if ((propertyDescriptor.getReadMethod() == null) && (read == true)) {
                    add = false;
                }
                if ((propertyDescriptor.getWriteMethod() == null) && (write == true)) {
                    add = false;
                }
                if (metaData.getType().getName().equals("java.lang.Class")) {
                    //Nunca se añaden propiedades que retornan Objetos "java.lang.Class" pq no son serializables por JSON
                    add = false;
                }

                if (add == true) {
                    propertiesMetaData.put(propertyName, metaData);
                }
            }
            return propertiesMetaData;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public MetaData getPropertyMetaData(String propertyName) {
        MetaData metaData;
        
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

        if (rigthPropertyName != null) {
            metaData=getPropertiesMetaData().get(leftPropertyName).getPropertyMetaData(rigthPropertyName);
        } else {
            metaData=getPropertiesMetaData().get(leftPropertyName);
        }

        
        return metaData;
    }
    
    @Override
    public String getPrimaryKeyPropertyName() {
        return null;
    }

    @Override
    public List<String> getNaturalKeyPropertiesName() {
        return new ArrayList<String>();
    }

    @Override
    public boolean isCollection() {
        if (collectionType != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public CollectionType getCollectionType() {
        return collectionType;
    }

    private Class getCollectionClass(Method method) {
        //http://stackoverflow.com/questions/1942644/get-generic-type-of-java-util-list

        Class collectionClass;

        if (method == null) {
            collectionClass = Object.class;
        } else {

            Class returnClass = method.getReturnType();
            if (List.class.isAssignableFrom(returnClass) || Set.class.isAssignableFrom(returnClass)) {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) returnType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes[0] instanceof Class) {
                        collectionClass = (Class) argTypes[0];
                    } else {
                        collectionClass = Object.class;
                    }
                } else {
                    collectionClass = Object.class;
                }
            } else if (Map.class.isAssignableFrom(returnClass)) {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) returnType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes[1] instanceof Class) {
                        collectionClass = (Class) argTypes[1];
                    } else {
                        collectionClass = Object.class;
                    }
                } else {
                    collectionClass = Object.class;
                }
            } else {
                throw new RuntimeException("El método no retorna un Map o una coleccion" + method.getName());
            }
        }
        return collectionClass;
    }

    @Override
    public String getPropertyName() {
        return this.propertyName;
    }

    @Override
    public String getPropertyPath() {
        return propertyPath;
    }

    
    
    @Override
    public String getLabel() {
        return getPropertyName();
    }

    @Override
    public Constraints getConstraints() {
        return new ConstraintsImpl();
    }

    class ConstraintsImpl implements Constraints {

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public long getMinimum() {
            return Integer.MIN_VALUE;
        }

        @Override
        public long getMaximum() {
            return Long.MAX_VALUE;
        }

        @Override
        public int getMinLength() {
            return 0;
        }

        @Override
        public int getMaxLength() {
            return Integer.MAX_VALUE;
        }

        @Override
        public String getPattern() {
            return null;
        }

        @Override
        public Format getFormat() {
            return null;
        }

        @Override
        public ValuesList getValuesList() {
            //En un Bean nunca hay ValueList así que siempre será null.
            return null;
        }

    }

}
