/*
 * Copyright 2012 alumno.
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
package es.logongas.ix3.persistence.impl.hibernate.metadata;

import es.logongas.ix3.persistence.services.annotations.Date;
import es.logongas.ix3.persistence.services.annotations.Time;
import es.logongas.ix3.persistence.services.annotations.Caption;
import es.logongas.ix3.persistence.services.metadata.Constraints;
import es.logongas.ix3.persistence.services.metadata.Format;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaType;
import es.logongas.ix3.util.ReflectionAnnotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Constraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

/**
 *
 * @author alumno
 */
public class MetaDataImplHibernate implements MetaData {

    //No usar directamente esta propiedad sino usar el método getPropertiesMetaData()
    private Map<String, MetaData> metaDatas = null;

    private final SessionFactory sessionFactory;
    private final Class entityType;
    private final Type type;
    private final String propertyName;
    private final MetaData parentMetaData;
    private String caption;
    private final ContraintsImpl constraints=new ContraintsImpl();


    protected MetaDataImplHibernate(Class entityType, SessionFactory sessionFactory, String propertyName, MetaData parentMetaData) {
        this.sessionFactory = sessionFactory;
        this.entityType = entityType;
        this.type = null;
        this.propertyName = propertyName;
        this.parentMetaData = parentMetaData;
        analizeAnotations();
    }

    protected MetaDataImplHibernate(Type type, SessionFactory sessionFactory, String propertyName, MetaData parentMetaData) {
        this.sessionFactory = sessionFactory;
        this.entityType = null;
        this.type = type;
        this.propertyName = propertyName;
        this.parentMetaData = parentMetaData;
        analizeAnotations();
    }

    @Override
    public Class getType() {
        if (isCollection()) {
            return ((org.hibernate.type.CollectionType) type).getElementType((SessionFactoryImplementor) this.sessionFactory).getReturnedClass();
        } else if (entityType != null) {
            return entityType;
        } else if (type != null) {
            return type.getReturnedClass();
        } else {
            throw new RuntimeException("No existe el tipo");
        }
    }

    @Override
    public boolean isCollection() {
        if (type != null) {
            return type.isCollectionType();
        } else {
            return false;
        }
    }

    @Override
    public boolean isCollectionLazy() {
        if (isCollection() == false) {
            return false;
        }

        org.hibernate.type.CollectionType collectionType = (org.hibernate.type.CollectionType) type;
        String role = collectionType.getRole();
        CollectionMetadata collectionMetadata = sessionFactory.getCollectionMetadata(role);

        return collectionMetadata.isLazy();
    }

    @Override
    public MetaType getMetaType() {
        if ((getPropertiesMetaData() == null) || (getPropertiesMetaData().size() == 0)) {
            return MetaType.Scalar;
        } else if (this.getPrimaryKeyPropertyName() != null) {
            return MetaType.Entity;
        } else {
            return MetaType.Component;
        }
    }

    @Override
    public es.logongas.ix3.persistence.services.metadata.CollectionType getCollectionType() {
        ClassMetadata classMetadata = getClassMetadata();
        if (classMetadata == null) {
            throw new RuntimeException("No existen los metadatos");
        }

        if (type instanceof SetType) {
            return es.logongas.ix3.persistence.services.metadata.CollectionType.Set;
        } else if (type instanceof ListType) {
            return es.logongas.ix3.persistence.services.metadata.CollectionType.List;
        } else if (type instanceof MapType) {
            return es.logongas.ix3.persistence.services.metadata.CollectionType.Map;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, MetaData> getPropertiesMetaData() {

        if (metaDatas == null) {
            metaDatas = new MetaDatas();

            ClassMetadata classMetadata = getClassMetadata();

            if (classMetadata != null) {
                //Añadimos la clave primaria al Map
                if (classMetadata.hasIdentifierProperty() == true) {
                    String propertyName = classMetadata.getIdentifierPropertyName();
                    Type propertyType = classMetadata.getIdentifierType();
                    MetaData metaData = new MetaDataImplHibernate(propertyType, sessionFactory, propertyName, this);
                    metaDatas.put(propertyName, metaData);
                }

                String[] propertyNames = classMetadata.getPropertyNames();
                for (String propertyName : propertyNames) {
                    Type propertyType = classMetadata.getPropertyType(propertyName);
                    MetaData metaData = new MetaDataImplHibernate(propertyType, sessionFactory, propertyName, this);
                    metaDatas.put(propertyName, metaData);
                }
            }
        }

        return metaDatas;
    }

    @Override
    public List<String> getNaturalKeyPropertiesName() {

        List<String> naturalKeyPropertiesName = new ArrayList<String>();

        ClassMetadata classMetadata = getClassMetadata();
        if (classMetadata == null) {
            return naturalKeyPropertiesName;
        }

        if (classMetadata.hasNaturalIdentifier()) {

            int[] positions = classMetadata.getNaturalIdentifierProperties();
            String[] propertyNames = classMetadata.getPropertyNames();

            for (int i = 0; i < positions.length; i++) {
                int position = positions[i];
                String naturalKeyPropertyName = propertyNames[position];

                naturalKeyPropertiesName.add(naturalKeyPropertyName);
            }

        } else {
            //Si no hay clave natural, la lista no tendrá ningún elemento
        }

        return naturalKeyPropertiesName;
    }

    @Override
    public String getPrimaryKeyPropertyName() {
        ClassMetadata classMetadata = getClassMetadata();
        if (classMetadata == null) {
            return null;
        }

        if (classMetadata.hasIdentifierProperty() == true) {
            return classMetadata.getIdentifierPropertyName();
        } else {
            return null;
        }
    }

    private ClassMetadata getClassMetadata() {
        return sessionFactory.getClassMetadata(this.getType());
    }

    @Override
    public String getCaption() {
        return this.caption;
    }

    @Override
    public String getPropertyName() {
        return this.propertyName;
    }

    @Override
    public Constraints getConstraints() {
        return constraints;
    }    
    
    private void analizeAnotations() {
        Class clazz;
        if (parentMetaData != null) {
            clazz = parentMetaData.getType();
        } else {
            clazz = null;
        }

        Caption captionAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Caption.class);
        if (captionAnnotation != null) {
            caption = captionAnnotation.value();
        } else {
            caption = getPropertyName();
        }

        constraints.format = null;

        Email emailAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Email.class);
        if (emailAnnotation != null) {
            if (constraints.format != null) {
                throw new RuntimeException("No se puede incluir la anotación Email porque ya tiene el formato:" + constraints.format);
            }

            constraints.format = Format.EMAIL;
        }

        URL urlAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), URL.class);
        if (urlAnnotation != null) {
            if (constraints.format != null) {
                throw new RuntimeException("No se puede incluir la anotación URL porque ya tiene el formato:" + constraints.format);
            }

            constraints.format = Format.URL;
        }

        Date dateAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Date.class);
        if (dateAnnotation != null) {
            if (constraints.format != null) {
                throw new RuntimeException("No se puede incluir la anotación Date porque ya tiene el formato:" + constraints.format);
            }

            constraints.format = Format.DATE;
        }

        Time timeAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Time.class);
        if (timeAnnotation != null) {
            if (constraints.format != null) {
                throw new RuntimeException("No se puede incluir la anotación Time porque ya tiene el formato:" + constraints.format);
            }

            constraints.format = Format.TIME;
        }

        Min minAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Min.class);
        if (minAnnotation != null) {
            constraints.minimum = minAnnotation.value();
        } else {
            constraints.minimum = Long.MIN_VALUE;
        }

        Max maxAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Max.class);
        if (maxAnnotation != null) {
            constraints.maximum = maxAnnotation.value();
        } else {
            constraints.maximum = Long.MAX_VALUE;
        }

        Size sizeAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Size.class);
        if (sizeAnnotation != null) {
            constraints.minLength = sizeAnnotation.min();
            constraints.maxLength = sizeAnnotation.max();

        } else {
            constraints.minLength = 0;
            constraints.maxLength = Integer.MAX_VALUE;

        }

        Pattern patternAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), Pattern.class);
        if (patternAnnotation != null) {
            constraints.pattern = patternAnnotation.regexp();
        } else {
            constraints.pattern = null;
        }

        NotBlank notBlankAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), NotBlank.class);
        NotEmpty notEmptyAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), NotEmpty.class);
        NotNull notNullAnnotation = ReflectionAnnotation.getAnnotation(clazz, getPropertyName(), NotNull.class);
        if ((notBlankAnnotation != null) || (notEmptyAnnotation != null) || (notNullAnnotation != null)) {
            constraints.required = true;
        } else {
            constraints.required = false;
        }

    }

    class ContraintsImpl implements Constraints {

        public boolean required;
        public long minimum;
        public long maximum;
        public int minLength;
        public int maxLength;
        public String pattern;
        public Format format;

        @Override
        public boolean isRequired() {
            return this.required;
        }

        @Override
        public long getMinimum() {
            return this.minimum;
        }

        @Override
        public long getMaximum() {
            return this.maximum;
        }

        @Override
        public int getMinLength() {
            return this.minLength;
        }

        @Override
        public int getMaxLength() {
            return this.maxLength;
        }

        @Override
        public String getPattern() {
            return this.pattern;
        }

        @Override
        public Format getFormat() {
            return this.format;
        }

    }

}
