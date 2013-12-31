/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.web.impl.json;

import es.logongas.ix3.persistence.services.metadata.CollectionType;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.persistence.services.metadata.MetaType;
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

    public MetaDataImplBean(Class clazz, CollectionType collectionType, boolean read, boolean write, MetaDataFactory metaDataFactory) {
        this.clazz = clazz;
        this.collectionType = collectionType;
        this.read = read;
        this.write = write;
        this.metaDataFactory = metaDataFactory;
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

                    if (propertyClass.isAssignableFrom(Set.class)) {
                        newCollectionType = CollectionType.Set;
                        realPropertyClass = getCollectionClass(propertyDescriptor.getReadMethod());
                        metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory);
                    } else if (propertyClass.isAssignableFrom(List.class)) {
                        newCollectionType = CollectionType.List;
                        realPropertyClass = getCollectionClass(propertyDescriptor.getReadMethod());
                        metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory);
                    } else if (propertyClass.isAssignableFrom(Map.class)) {
                        newCollectionType = CollectionType.Map;
                        realPropertyClass = getCollectionClass(propertyDescriptor.getReadMethod());
                        metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory);
                    } else {
                        //No es una colección
                        newCollectionType = null;
                        realPropertyClass = propertyClass;

                        metaData = metaDataFactory.getMetaData(realPropertyClass);
                        if (metaData == null) {
                            metaData = new MetaDataImplBean(realPropertyClass, newCollectionType, read, write, metaDataFactory);
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
    public boolean isCollectionLazy() {
        return false;
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
            if (returnClass.isAssignableFrom(List.class) || returnClass.isAssignableFrom(Set.class)) {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) returnType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    collectionClass = (Class) argTypes[0];
                } else {
                    collectionClass = Object.class;
                }
            } else if (returnClass.isAssignableFrom(Map.class)) {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) returnType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    collectionClass = (Class) argTypes[1];
                } else {
                    collectionClass = Object.class;
                }
            } else {
                throw new RuntimeException("El método no retorna un Map o una coleccion" + method.getName());
            }
        }
        return collectionClass;
    }
}
