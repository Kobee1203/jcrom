/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrProperty
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class PropertyMapper {

    private final Mapper mapper;

    public PropertyMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    void mapPropertiesToMap(Object obj, Field field, Class<?> valueType, PropertyIterator propIterator, boolean ignoreReadOnlyProperties) throws RepositoryException, IOException, IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        while (propIterator.hasNext()) {
            Property p = propIterator.nextProperty();
            // we ignore the read-only properties added by the repository
            if (!ignoreReadOnlyProperties || (!p.getName().startsWith("jcr:") && !p.getName().startsWith(NamespaceRegistry.NAMESPACE_JCR) && !p.getName().startsWith("nt:") && !p.getName().startsWith(NamespaceRegistry.NAMESPACE_NT))) {
                if (valueType.isArray()) {
                    if (p.getDefinition().isMultiple()) {
                        map.put(p.getName(), valuesToArray(valueType.getComponentType(), p.getValues()));
                    } else {
                        Value[] values = new Value[1];
                        values[0] = p.getValue();
                        map.put(p.getName(), valuesToArray(valueType.getComponentType(), values));
                    }
                } else {
                    map.put(p.getName(), getValue(valueType, p.getValue()));
                }
            }
        }
        field.set(obj, map);
    }

    private Object[] valuesToArray(Class<?> type, Value[] values) throws RepositoryException, IOException {
        Object[] arr = (Object[]) Array.newInstance(type, values.length);
        for (int i = 0; i < values.length; i++) {
            arr[i] = getValue(type, values[i]);
        }
        return arr;
    }

    void mapSerializedPropertyToField(Object obj, Field field, Node node) throws RepositoryException, IOException, IllegalAccessException, ClassNotFoundException {

        String propertyName = getSerializedPropertyName(field);
        if (node.hasProperty(propertyName)) {
            Property p = node.getProperty(propertyName);
            //field.set(obj, deserialize(p.getStream()));
            field.set(obj, deserialize(p.getBinary().getStream()));
        }
    }

    /**
     * Parses given locale string to Locale object. If the string is empty or null then the we return null.
     * 
     * @param localeString
     *            a string containing locale in <code>language_country_variant</code> format.
     * @return Locale
     */
    private Locale parseLocale(String localeString) {
        if (localeString != null && localeString.length() > 0) {
            StringTokenizer st = new StringTokenizer(localeString, "_");
            String language = st.hasMoreElements() ? st.nextToken() : Locale.getDefault().getLanguage();
            String country = st.hasMoreElements() ? st.nextToken() : "";
            String variant = st.hasMoreElements() ? st.nextToken() : "";
            return new Locale(language, country, variant);
        }
        return null;
    }

    Value createValue(Class<?> c, Object fieldValue, ValueFactory valueFactory) throws RepositoryException {
        if (c == String.class) {
            return valueFactory.createValue((String) fieldValue);
        } else if (c == Date.class) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) fieldValue);
            return valueFactory.createValue(cal);
        } else if (c == Timestamp.class) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Timestamp) fieldValue).getTime());
            return valueFactory.createValue(cal);
        } else if (c == Calendar.class) {
            return valueFactory.createValue((Calendar) fieldValue);
        } else if (c == InputStream.class) {
            //return valueFactory.createValue((InputStream) fieldValue);
            Binary binary = valueFactory.createBinary((InputStream) fieldValue);
            return valueFactory.createValue(binary);
        } else if (c.isArray() && c.getComponentType() == byte.class) {
            //return valueFactory.createValue(new ByteArrayInputStream((byte[]) fieldValue));
            Binary binary = valueFactory.createBinary(new ByteArrayInputStream((byte[]) fieldValue));
            return valueFactory.createValue(binary);
        } else if (c == Integer.class || c == int.class) {
            return valueFactory.createValue((Integer) fieldValue);
        } else if (c == Long.class || c == long.class) {
            return valueFactory.createValue((Long) fieldValue);
        } else if (c == Double.class || c == double.class) {
            return valueFactory.createValue((Double) fieldValue);
        } else if (c == Boolean.class || c == boolean.class) {
            return valueFactory.createValue((Boolean) fieldValue);
        } else if (c == Locale.class) {
            return valueFactory.createValue(((Locale) fieldValue).toString());
        } else if (c.isEnum()) {
            return valueFactory.createValue(((Enum<?>) fieldValue).name());
        }
        return null;
    }

    Object getValue(Class<?> c, Value value) throws RepositoryException, IOException {
        if (c == String.class) {
            return value.getString();
        } else if (c == Date.class) {
            return value.getDate().getTime();
        } else if (c == Timestamp.class) {
            return new Timestamp(value.getDate().getTimeInMillis());
        } else if (c == Calendar.class) {
            return value.getDate();
        } else if (c == InputStream.class) {
            //return value.getStream();
            return value.getBinary().getStream();
        } else if (c.isArray() && c.getComponentType() == byte.class) {
            // byte array...we need to read from the stream
            //return Mapper.readBytes(value.getStream());
            return Mapper.readBytes(value.getBinary().getStream());
        } else if (c == Integer.class || c == int.class) {
            return (int) value.getDouble();
        } else if (c == Long.class || c == long.class) {
            return value.getLong();
        } else if (c == Double.class || c == double.class) {
            return value.getDouble();
        } else if (c == Boolean.class || c == boolean.class) {
            return value.getBoolean();
        } else if (c == Locale.class) {
            return parseLocale(value.getString());
        } else if (c.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) c, value.getString());
        }
        return null;
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

    String getProtectedPropertyName(Field field) {
        JcrProtectedProperty jcrProperty = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrProtectedProperty.class);
        String name = field.getName();
        if (!jcrProperty.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            name = jcrProperty.name();
        }
        return name;
    }

    void mapPropertyToField(Object obj, Field field, Node node) throws RepositoryException, IllegalAccessException, IOException {
        String name = getPropertyName(field);

        if (ReflectionUtils.implementsInterface(field.getType(), Map.class)) {
            // map of properties
            Class<?> valueType = ReflectionUtils.getParameterizedClass(field, 1);
            try {
				Node childrenContainer = node.getNode(name);
				PropertyIterator propIterator = childrenContainer.getProperties();
				mapPropertiesToMap(obj, field, valueType, propIterator, true);
            } catch (PathNotFoundException pne) {
				// ignore here as the Field could have been added to the model
				// since the Node was created and not yet been populated.
			}
        } else {
            mapToField(name, field, obj, node);
        }
    }

    void mapProtectedPropertyToField(Object obj, Field field, Node node) throws RepositoryException, IllegalAccessException, IOException {
        String name = getProtectedPropertyName(field);

        if (ReflectionUtils.implementsInterface(field.getType(), Map.class)) {
            // map of properties
            Class<?> valueType = ReflectionUtils.getParameterizedClass(field, 1);
            Node childrenContainer = node.getNode(name);
            PropertyIterator propIterator = childrenContainer.getProperties();
            mapPropertiesToMap(obj, field, valueType, propIterator, false);
        } else {
            mapToField(name, field, obj, node);
        }
    }

    void mapToField(String propertyName, Field field, Object obj, Node node) throws RepositoryException, IllegalAccessException, IOException {
        if (node.hasProperty(propertyName)) {
            Property p = node.getProperty(propertyName);

            if (ReflectionUtils.implementsInterface(field.getType(), List.class)) {
                // multi-value property (List)
                List<Object> properties = new ArrayList<Object>();
                Class<?> paramClass = ReflectionUtils.getParameterizedClass(field);
                for (Value value : p.getValues()) {
                    properties.add(getValue(paramClass, value));
                }
                field.set(obj, properties);

            } else if (field.getType().isArray() && field.getType().getComponentType() != byte.class) {
                // multi-value property (array)
                Value[] values = p.getValues();
                if (field.getType().getComponentType() == int.class) {
                    int[] arr = new int[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = (int) values[i].getDouble();
                    }
                    field.set(obj, arr);
                } else if (field.getType().getComponentType() == long.class) {
                    long[] arr = new long[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = values[i].getLong();
                    }
                    field.set(obj, arr);
                } else if (field.getType().getComponentType() == double.class) {
                    double[] arr = new double[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = values[i].getDouble();
                    }
                    field.set(obj, arr);
                } else if (field.getType().getComponentType() == boolean.class) {
                    boolean[] arr = new boolean[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = values[i].getBoolean();
                    }
                    field.set(obj, arr);
                } else if (field.getType().getComponentType() == Locale.class) {
                    Locale[] arr = new Locale[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = parseLocale(values[i].getString());
                    }
                    field.set(obj, arr);
                } else {
                    Object[] arr = valuesToArray(field.getType().getComponentType(), values);
                    field.set(obj, arr);
                }

            } else {
                // single-value property
                field.set(obj, getValue(field.getType(), p.getValue()));
            }
        }
    }

    void mapSerializedFieldToProperty(Field field, Object obj, Node node) throws IllegalAccessException, RepositoryException, IOException {

        String propertyName = getSerializedPropertyName(field);
        Object fieldValue = field.get(obj);
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

    @SuppressWarnings("unchecked")
    private void addChildMap(Field field, Object obj, Node node, String nodeName, Mapper mapper) throws RepositoryException, IllegalAccessException {

        Map<String, Object> map = (Map<String, Object>) field.get(obj);
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
                mapToProperty(entry.getKey(), ReflectionUtils.getParameterizedClass(field, 1), null, entry.getValue(), childContainer);
            }
        }
    }

    void mapFieldToProperty(Field field, Object obj, Node node, Mapper mapper) throws RepositoryException, IllegalAccessException {

        String name = getPropertyName(field);
        if (ReflectionUtils.implementsInterface(field.getType(), Map.class)) {
            // this is a Map child, where we map the key/value pairs as properties
            addChildMap(field, obj, node, name, mapper);
        } else {
            // normal property
            Class<?> paramClass = ReflectionUtils.implementsInterface(field.getType(), List.class) ? ReflectionUtils.getParameterizedClass(field) : null;
            mapToProperty(name, field.getType(), paramClass, field.get(obj), node);
        }
    }

    void mapToProperty(String propertyName, Class<?> type, Class<?> paramClass, Object propertyValue, Node node) throws RepositoryException {

        // make sure that the field value is not null
        if (propertyValue != null) {

            ValueFactory valueFactory = node.getSession().getValueFactory();

            if (ReflectionUtils.implementsInterface(type, List.class)) {
                // multi-value property List
                List<?> fieldValues = (List<?>) propertyValue;
                if (!fieldValues.isEmpty()) {
                    Value[] values = new Value[fieldValues.size()];
                    for (int i = 0; i < fieldValues.size(); i++) {
                        values[i] = createValue(paramClass, fieldValues.get(i), valueFactory);
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
                        values[i] = createValue(int.class, ints[i], valueFactory);
                    }
                } else if (type.getComponentType() == long.class) {
                    long[] longs = (long[]) propertyValue;
                    values = new Value[longs.length];
                    for (int i = 0; i < longs.length; i++) {
                        values[i] = createValue(long.class, longs[i], valueFactory);
                    }
                } else if (type.getComponentType() == double.class) {
                    double[] doubles = (double[]) propertyValue;
                    values = new Value[doubles.length];
                    for (int i = 0; i < doubles.length; i++) {
                        values[i] = createValue(double.class, doubles[i], valueFactory);
                    }
                } else if (type.getComponentType() == boolean.class) {
                    boolean[] booleans = (boolean[]) propertyValue;
                    values = new Value[booleans.length];
                    for (int i = 0; i < booleans.length; i++) {
                        values[i] = createValue(boolean.class, booleans[i], valueFactory);
                    }
                } else if (type.getComponentType() == Locale.class) {
                    Locale[] locales = (Locale[]) propertyValue;
                    values = new Value[locales.length];
                    for (int i = 0; i < locales.length; i++) {
                        values[i] = createValue(Locale.class, locales[i], valueFactory);
                    }
                } else {
                    // Object
                    Object[] objects = (Object[]) propertyValue;
                    values = new Value[objects.length];
                    for (int i = 0; i < objects.length; i++) {
                        values[i] = createValue(type.getComponentType(), objects[i], valueFactory);
                    }

                }
                node.setProperty(propertyName, values);

            } else {
                // single-value property
                Value value = createValue(type, propertyValue, valueFactory);
                if (value != null) {
                    node.setProperty(propertyName, value);
                }
            }
        } else {
            // remove the value
            if (ReflectionUtils.implementsInterface(type, List.class)) {
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
