/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
 * Authors: Olafur Gauti Gudmundsson, Nicolas Dos Santos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrProtectedProperty;
import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.converter.Converter;
import org.jcrom.converter.DefaultConverter;
import org.jcrom.type.TypeHandler;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrProperty
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class PropertyMapper {

    private final Mapper mapper;

    private final TypeHandler typeHandler;

    public PropertyMapper(Mapper mapper) {
        this.mapper = mapper;
        this.typeHandler = mapper.getTypeHandler();
    }

    void mapPropertiesToMap(String propertyName, Field field, Object obj, Node node, Class<? extends Converter<?, ?>> converterClass, boolean ignoreReadOnlyProperties) throws RepositoryException, IOException, IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();

        Type genericType = field.getGenericType();

        if (converterClass != null) {
            genericType = ReflectionUtils.getConverterGenericType(converterClass, 1);
        }

        Class<?> valueType = ReflectionUtils.getParameterizedClass(genericType, 1);
        Node childrenContainer = node.getNode(propertyName);
        PropertyIterator propIterator = childrenContainer.getProperties();

        while (propIterator.hasNext()) {
            Property p = propIterator.nextProperty();
            // we ignore the read-only properties added by the repository
            if (!ignoreReadOnlyProperties || (!p.getName().startsWith("jcr:") && !p.getName().startsWith(NamespaceRegistry.NAMESPACE_JCR) && !p.getName().startsWith("nt:") && !p.getName().startsWith(NamespaceRegistry.NAMESPACE_NT))) {
                if (valueType.isArray()) {
                    if (p.getDefinition().isMultiple()) {
                        map.put(p.getName(), typeHandler.getValues(valueType.getComponentType(), null, p.getValues(), null));
                    } else {
                        Value[] values = new Value[1];
                        values[0] = p.getValue();
                        map.put(p.getName(), typeHandler.getValues(valueType.getComponentType(), null, values, null));
                    }
                } else {
                    map.put(p.getName(), typeHandler.getValue(valueType, genericType, p.getValue(), null));
                }
            }
        }

        Object fieldValue = map;

        if (converterClass != null) {
            try {
                Converter converter = converterClass.newInstance();
                fieldValue = converter.convertToEntityAttribute(fieldValue);
            } catch (InstantiationException e) {
                throw new IllegalAccessException("Could not instantiate the Converter object from the field '" + field.getName() + "'");
            }
        }

        typeHandler.setObject(field, obj, fieldValue);
    }

    void mapSerializedPropertyToField(Object obj, Field field, Node node, int depth, NodeFilter nodeFilter) throws RepositoryException, IOException, IllegalAccessException, ClassNotFoundException {
        String propertyName = getSerializedPropertyName(field);

        if (nodeFilter == null || nodeFilter.isIncluded(NodeFilter.PROPERTY_PREFIX + field.getName(), node, depth)) {
            if (node.hasProperty(propertyName)) {
                Property p = node.getProperty(propertyName);
                //field.set(obj, deserialize(p.getStream()));
                typeHandler.setObject(field, obj, deserialize(p.getBinary().getStream()));
            }
        }
    }

    String getSerializedPropertyName(Field field) {
        JcrSerializedProperty jcrProperty = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrSerializedProperty.class);
        String propertyName = field.getName();
        if (!jcrProperty.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            propertyName = jcrProperty.name();
        }
        return propertyName;
    }

    String getPropertyName(Field field) {
        JcrProperty jcrProperty = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrProperty.class);
        String name = field.getName();
        if (!jcrProperty.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            name = jcrProperty.name();
        }
        return name;
    }

    Class<? extends Converter<?, ?>> getPropertyConverter(Field field) {
        JcrProperty jcrProperty = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrProperty.class);
        Class<? extends Converter<?, ?>> converter = null;
        if (!DefaultConverter.class.equals(jcrProperty.converter())) {
            converter = jcrProperty.converter();
        }
        return converter;
    }

    String getProtectedPropertyName(Field field) {
        JcrProtectedProperty jcrProperty = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrProtectedProperty.class);
        String name = field.getName();
        if (!jcrProperty.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            name = jcrProperty.name();
        }
        return name;
    }

    void mapPropertyToField(Object obj, Field field, Node node, int depth, NodeFilter nodeFilter) throws RepositoryException, IllegalAccessException, IOException {
        String name = getPropertyName(field);

        if (nodeFilter == null || nodeFilter.isIncluded(NodeFilter.PROPERTY_PREFIX + field.getName(), node, depth)) {
            Class<?> type = field.getType();

            Class<? extends Converter<?, ?>> converterClass = getPropertyConverter(field);
            if (converterClass != null) {
                type = ReflectionUtils.getParameterizedClass(converterClass.getGenericInterfaces()[0], 1);
            }

            if (typeHandler.isMap(type)) {
                // map of properties
                try {
                    mapPropertiesToMap(name, field, obj, node, converterClass, true);
                } catch (PathNotFoundException pne) {
                    // ignore here as the Field could have been added to the model
                    // since the Node was created and not yet been populated.
                }
            } else {
                mapToField(name, field, obj, node, converterClass);
            }
        }
    }

    void mapProtectedPropertyToField(Object obj, Field field, Node node) throws RepositoryException, IllegalAccessException, IOException {
        String name = getProtectedPropertyName(field);

        if (typeHandler.isMap(field.getType())) {
            // map of properties
            mapPropertiesToMap(name, field, obj, node, null, false);
        } else {
            mapToField(name, field, obj, node, null);
        }
    }

    void mapToField(String propertyName, Field field, Object obj, Node node, Class<? extends Converter<?, ?>> converterClass) throws RepositoryException, IllegalAccessException, IOException {
        if (node.hasProperty(propertyName)) {
            Property p = node.getProperty(propertyName);

            Class<?> type = field.getType();
            Type genericType = field.getGenericType();

            if (converterClass != null) {
                type = ReflectionUtils.getParameterizedClass(converterClass.getGenericInterfaces()[0], 1);
                genericType = ReflectionUtils.getConverterGenericType(converterClass, 1);
            }

            Object currentValue = typeHandler.getObject(field, obj);

            Object fieldValue;
            if (p.isMultiple()) {
                fieldValue = typeHandler.getValues(type, genericType, p.getValues(), currentValue);
            } else {
                fieldValue = typeHandler.getValue(type, genericType, p.getValue(), currentValue);
            }

            if (converterClass != null) {
                try {
                    Converter converter = converterClass.newInstance();
                    fieldValue = converter.convertToEntityAttribute(fieldValue);
                } catch (InstantiationException e) {
                    throw new IllegalAccessException("Could not instantiate the Converter object from the field '" + field.getName() + "'");
                }
            }

            typeHandler.setObject(field, obj, fieldValue);
        }
    }

    private void mapSerializedFieldToProperty(Field field, Object obj, Node node, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        String propertyName = getSerializedPropertyName(field);
        Object fieldValue = typeHandler.getObject(field, obj);
        // make sure that this property is supposed to be updated
        if (nodeFilter == null || nodeFilter.isIncluded(NodeFilter.PROPERTY_PREFIX + field.getName(), node, depth)) {
            if (fieldValue != null) {
                // serialize and store
                //node.setProperty(propertyName, new ByteArrayInputStream(serialize(fieldValue)));
                ValueFactory valueFactory = node.getSession().getValueFactory();
                Binary binary = valueFactory.createBinary(new ByteArrayInputStream(serialize(fieldValue)));
                node.setProperty(propertyName, binary);
            } else {
                // remove the value
                node.setProperty(propertyName, (Value) null);
            }
        }
    }

    void addSerializedProperty(Field field, Object obj, Node node) throws RepositoryException, IllegalAccessException, IOException {
        mapSerializedFieldToProperty(field, obj, node, NodeFilter.DEPTH_INFINITE, null);
    }

    void updateSerializedProperty(Field field, Object obj, Node node, int depth, NodeFilter nodeFilter) throws RepositoryException, IllegalAccessException, IOException {
        mapSerializedFieldToProperty(field, obj, node, depth, nodeFilter);
    }

    private void addChildMap(String nodeName, Class<?> paramClass, Map<String, Object> map, Node node, Mapper mapper) throws RepositoryException, IllegalAccessException {

        boolean nullOrEmpty = map == null || map.isEmpty();
        // remove the child node
        NodeIterator nodeIterator = node.getNodes(nodeName);
        while (nodeIterator.hasNext()) {
            nodeIterator.nextNode().remove();
        }
        // add the map as a child node
        Node childContainer = node.addNode(mapper.getCleanName(nodeName));
        if (!nullOrEmpty) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                mapToProperty(entry.getKey(), paramClass, null, entry.getValue(), childContainer);
            }
        }
    }

    private void mapFieldToProperty(Field field, Object obj, Node node, int depth, NodeFilter nodeFilter, Mapper mapper) throws RepositoryException, IllegalAccessException {

        String name = getPropertyName(field);
        // make sure that this property is supposed to be updated
        if (nodeFilter == null || nodeFilter.isIncluded(NodeFilter.PROPERTY_PREFIX + field.getName(), node, depth)) {
            Class<?> type = field.getType();
            Type genericType = field.getGenericType();
            Object value = field.get(obj);

            Class<? extends Converter<?, ?>> converterClass = getPropertyConverter(field);
            if (converterClass != null) {
                type = ReflectionUtils.getParameterizedClass(converterClass.getGenericInterfaces()[0], 1);
                genericType = ReflectionUtils.getConverterGenericType(converterClass, 1);
                try {
                    Converter converter = converterClass.newInstance();
                    value = converter.convertToJcrProperty(value);
                } catch (InstantiationException e) {
                    throw new IllegalAccessException("Could not instantiate the Converter object from the field '" + name + "'");
                }
            }

            if (typeHandler.isMap(type)) {
                // this is a Map child, where we map the key/value pairs as properties
                Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType, 1);
                addChildMap(name, paramClass, (Map<String, Object>) value, node, mapper);
                // addChildMap(field, obj, node, name, mapper);
            } else {
                // normal property
                Class<?> paramClass = typeHandler.isList(type) ? ReflectionUtils.getParameterizedClass(genericType) : null;
                mapToProperty(name, type, paramClass, value, node);
            }
        }
    }

    void addProperty(Field field, Object obj, Node node, Mapper mapper) throws RepositoryException, IllegalAccessException {
        mapFieldToProperty(field, obj, node, NodeFilter.DEPTH_INFINITE, null, mapper);
    }

    void updateProperty(Field field, Object obj, Node node, int depth, NodeFilter nodeFilter, Mapper mapper) throws RepositoryException, IllegalAccessException {
        mapFieldToProperty(field, obj, node, depth, nodeFilter, mapper);
    }

    void mapToProperty(String propertyName, Class<?> type, Class<?> paramClass, Object propertyValue, Node node) throws RepositoryException {

        ValueFactory valueFactory = node.getSession().getValueFactory();

        boolean isMultiple = typeHandler.isList(type) || (type.isArray() && type.getComponentType() != byte.class);

        // make sure that the field value is not null
        if (propertyValue != null) {

            if (isMultiple) {
                node.setProperty(propertyName, typeHandler.createValues(type, paramClass, propertyValue, valueFactory));
            } else {
                Value value = typeHandler.createValue(type, propertyValue, valueFactory);
                if (value != null) {
                    node.setProperty(propertyName, value);
                }
            }

            /*
            if (typeHandler.isList(type)) {
                // multi-value property List
                List<?> fieldValues = (List<?>) propertyValue;
                if (!fieldValues.isEmpty()) {
                    Value[] values = new Value[fieldValues.size()];
                    for (int i = 0; i < fieldValues.size(); i++) {
                        values[i] = JcrUtils.createValue(paramClass, fieldValues.get(i), valueFactory);
                    }
                    node.setProperty(propertyName, values);
                } else {
                    node.setProperty(propertyName, new Value[0]);
                }

            } else if (type.isArray() && type.getComponentType() != byte.class) {
                // multi-value property array
                Value[] values;
                if (type.getComponentType() == int.class) {
                    int[] ints = (int[]) propertyValue;
                    values = new Value[ints.length];
                    for (int i = 0; i < ints.length; i++) {
                        values[i] = JcrUtils.createValue(int.class, ints[i], valueFactory);
                    }
                } else if (type.getComponentType() == long.class) {
                    long[] longs = (long[]) propertyValue;
                    values = new Value[longs.length];
                    for (int i = 0; i < longs.length; i++) {
                        values[i] = JcrUtils.createValue(long.class, longs[i], valueFactory);
                    }
                } else if (type.getComponentType() == double.class) {
                    double[] doubles = (double[]) propertyValue;
                    values = new Value[doubles.length];
                    for (int i = 0; i < doubles.length; i++) {
                        values[i] = JcrUtils.createValue(double.class, doubles[i], valueFactory);
                    }
                } else if (type.getComponentType() == boolean.class) {
                    boolean[] booleans = (boolean[]) propertyValue;
                    values = new Value[booleans.length];
                    for (int i = 0; i < booleans.length; i++) {
                        values[i] = JcrUtils.createValue(boolean.class, booleans[i], valueFactory);
                    }
                } else if (type.getComponentType() == Locale.class) {
                    Locale[] locales = (Locale[]) propertyValue;
                    values = new Value[locales.length];
                    for (int i = 0; i < locales.length; i++) {
                        values[i] = JcrUtils.createValue(Locale.class, locales[i], valueFactory);
                    }
                } else {
                    // Object
                    Object[] objects = (Object[]) propertyValue;
                    values = new Value[objects.length];
                    for (int i = 0; i < objects.length; i++) {
                        values[i] = JcrUtils.createValue(type.getComponentType(), objects[i], valueFactory);
                    }

                }
                node.setProperty(propertyName, values);

            } else {
                // single-value property
                Value value = JcrUtils.createValue(type, propertyValue, valueFactory);
                if (value != null) {
                    node.setProperty(propertyName, value);
                }
            }
            */
        } else {
            // remove the value
            if (isMultiple) {
                node.setProperty(propertyName, (Value[]) null);
            } else {
                node.setProperty(propertyName, (Value) null);
            }
        }
    }

    /**
     * Serialize an object to a byte array.
     * 
     * @param obj
     *            the object to be serialized
     * @return the serialized object
     * @throws java.lang.Exception
     */
    byte[] serialize(Object obj) throws IOException {
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        try {
            out.writeObject(obj);
        } finally {
            out.close();
        }

        // Get the bytes of the serialized object
        return bos.toByteArray();
    }

    /**
     * Deserialize an object from a byte array.
     * 
     * @param bytes
     * @return
     * @throws java.lang.Exception
     */
    private Object deserialize(InputStream byteStream) throws IOException, ClassNotFoundException {
        // Deserialize from a byte array
        ObjectInputStream in = new ObjectInputStream(byteStream);
        try {
            return in.readObject();
        } finally {
            in.close();
        }
    }

}
