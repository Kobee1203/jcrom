/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
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
package org.jcrom.type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.jcrom.annotations.JcrNode;
import org.jcrom.util.JcrUtils;
import org.jcrom.util.ReflectionUtils;
import org.jcrom.util.io.IOUtils;

/**
 * The default implementation of the interface {@link TypeHandler}.
 * 
 * @author Nicolas Dos Santos
 */
public class DefaultTypeHandler implements TypeHandler {

    @Override
    public Object resolveAddEntity(Object entity) {
        return entity;
    }

    @Override
    public JcrNode getJcrNodeAnnotation(Class<?> type, Type genericType, Object entity) {
        return ReflectionUtils.getJcrNodeAnnotation(type);
    }

    @Override
    public boolean isPropertyType(Class<?> type) {
        return isValidMapValueType(type) || type == InputStream.class || isArrayOfType(type, byte.class);
    }

    private static boolean isArrayOfType(Class<?> c, Class<?> type) {
        return c.isArray() && c.getComponentType() == type;
    }

    @Override
    public boolean isValidMapValueType(Class<?> type) {
        return type == String.class || isArrayOfType(type, String.class) || type == Date.class || isArrayOfType(type, Date.class) || type == Calendar.class || isArrayOfType(type, Calendar.class) || type == Timestamp.class || isArrayOfType(type, Timestamp.class) || type == Integer.class || isArrayOfType(type, Integer.class) || type == int.class || isArrayOfType(type, int.class) || type == Long.class || isArrayOfType(type, Long.class) || type == long.class || isArrayOfType(type, long.class) || type == Double.class || isArrayOfType(type, Double.class) || type == double.class || isArrayOfType(type, double.class) || type == Boolean.class || isArrayOfType(type, Boolean.class) || type == boolean.class || isArrayOfType(type, boolean.class) || type == Locale.class || isArrayOfType(type, Locale.class) || type.isEnum() || (type.isArray() && type.getComponentType().isEnum());
    }

    @Override
    public boolean isDateType(Class<?> type) {
        return type == Date.class || type == Calendar.class || type == Timestamp.class;
    }

    @Override
    public boolean isMap(Class<?> type) {
        return ReflectionUtils.implementsInterface(type, Map.class);
    }

    @Override
    public boolean isList(Class<?> type) {
        return ReflectionUtils.implementsInterface(type, List.class);
    }

    @Override
    public boolean isString(Class<?> type) {
        return type == String.class;
    }

    @Override
    public Class<?> getType(Class<?> type, Type genericType, Object source) {
        return type;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, Value value, Object currentObj) throws RepositoryException {
        Object fieldValue = null;

        if (value != null) {
            if (type == String.class) {
                fieldValue = value.getString();
            } else if (type == Date.class) {
                fieldValue = value.getDate().getTime();
            } else if (type == Timestamp.class) {
                fieldValue = new Timestamp(value.getDate().getTimeInMillis());
            } else if (type == Calendar.class) {
                fieldValue = value.getDate();
            } else if (type == InputStream.class) {
                //fieldValue = value.getStream();
                fieldValue = value.getBinary().getStream();
            } else if (type.isArray() && type.getComponentType() == byte.class) {
                // byte array...we need to read from the stream
                //fieldValue = Mapper.readBytes(value.getStream());
                try {
                    fieldValue = IOUtils.toByteArray(value.getBinary().getStream());
                } catch (IOException e) {
                    throw new RepositoryException("Could not the Value stream to byte array: " + e.getMessage(), e);
                }
            } else if (type == Integer.class || type == int.class) {
                fieldValue = (int) value.getDouble();
            } else if (type == Long.class || type == long.class) {
                fieldValue = value.getLong();
            } else if (type == Double.class || type == double.class) {
                fieldValue = value.getDouble();
            } else if (type == Boolean.class || type == boolean.class) {
                fieldValue = value.getBoolean();
            } else if (type == Locale.class) {
                fieldValue = JcrUtils.parseLocale(value.getString());
            } else if (type.isEnum()) {
                fieldValue = Enum.valueOf((Class<? extends Enum>) type, value.getString());
            }
        }

        return fieldValue;
    }

    @Override
    public Object getValues(Class<?> type, Type genericType, Value[] values, Object currentObj) throws RepositoryException {
        Object fieldValue = null;

        if (values != null) {
            if (isList(type)) {
                // multi-value property (List)
                List<Object> properties = new ArrayList<Object>();
                Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
                for (Value v : values) {
                    properties.add(getValue(paramClass, null, v, null));
                }
                fieldValue = properties;
            } else if (type.isArray() && type.getComponentType() != byte.class) {
                // multi-value property (array)
                if (type.getComponentType() == int.class) {
                    int[] arr = new int[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = (int) values[i].getDouble();
                    }
                    fieldValue = arr;
                } else if (type.getComponentType() == long.class) {
                    long[] arr = new long[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = values[i].getLong();
                    }
                    fieldValue = arr;
                } else if (type.getComponentType() == double.class) {
                    double[] arr = new double[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = values[i].getDouble();
                    }
                    fieldValue = arr;
                } else if (type.getComponentType() == boolean.class) {
                    boolean[] arr = new boolean[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = values[i].getBoolean();
                    }
                    fieldValue = arr;
                } else if (type.getComponentType() == Locale.class) {
                    Locale[] arr = new Locale[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = JcrUtils.parseLocale(values[i].getString());
                    }
                    fieldValue = arr;
                } else {
                    Object[] arr = valuesToArray(type.getComponentType(), genericType, values, currentObj);
                    fieldValue = arr;
                }
            } else {
                Object[] arr = valuesToArray(type, genericType, values, currentObj);
                fieldValue = arr;
            }
        }

        return fieldValue;
    }

    protected Object[] valuesToArray(Class<?> type, Type genericType, Value[] values, Object currentObj) throws RepositoryException {
        Object[] arr = (Object[]) Array.newInstance(type, values.length);
        for (int i = 0; i < values.length; i++) {
            arr[i] = getValue(type, null, values[i], null);
        }
        return arr;
    }

    @Override
    public Value createValue(Class<?> type, Object value, ValueFactory valueFactory) throws RepositoryException {
        Value nodeValue = null;

        if (type == String.class) {
            nodeValue = valueFactory.createValue((String) value);
        } else if (type == Date.class) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) value);
            nodeValue = valueFactory.createValue(cal);
        } else if (type == Timestamp.class) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Timestamp) value).getTime());
            nodeValue = valueFactory.createValue(cal);
        } else if (type == Calendar.class) {
            nodeValue = valueFactory.createValue((Calendar) value);
        } else if (type == InputStream.class) {
            //nodeValue = valueFactory.createValue((InputStream) fieldValue);
            Binary binary = valueFactory.createBinary((InputStream) value);
            nodeValue = valueFactory.createValue(binary);
        } else if (type.isArray() && type.getComponentType() == byte.class) {
            //nodeValue = valueFactory.createValue(new ByteArrayInputStream((byte[]) fieldValue));
            Binary binary = valueFactory.createBinary(new ByteArrayInputStream((byte[]) value));
            nodeValue = valueFactory.createValue(binary);
        } else if (type == Integer.class || type == int.class) {
            nodeValue = valueFactory.createValue((Integer) value);
        } else if (type == Long.class || type == long.class) {
            nodeValue = valueFactory.createValue((Long) value);
        } else if (type == Double.class || type == double.class) {
            nodeValue = valueFactory.createValue((Double) value);
        } else if (type == Boolean.class || type == boolean.class) {
            nodeValue = valueFactory.createValue((Boolean) value);
        } else if (type == Locale.class) {
            nodeValue = valueFactory.createValue(((Locale) value).toString());
        } else if (type.isEnum()) {
            nodeValue = valueFactory.createValue(((Enum<?>) value).name());
        }

        return nodeValue;
    }

    @Override
    public Value[] createValues(Class<?> type, Class<?> genericType, Object values, ValueFactory valueFactory) throws RepositoryException {
        Value[] nodeValues = null;

        if (isList(type)) {
            // multi-value property List
            List<?> fieldValues = (List<?>) values;
            if (!fieldValues.isEmpty()) {
                Value[] valuesArray = new Value[fieldValues.size()];
                for (int i = 0; i < fieldValues.size(); i++) {
                    valuesArray[i] = createValue(genericType, fieldValues.get(i), valueFactory);
                }
                nodeValues = valuesArray;
            } else {
                nodeValues = new Value[0];
            }
        } else if (type.isArray() && type.getComponentType() != byte.class) {
            // multi-value property array
            Value[] valuesArray;
            if (type.getComponentType() == int.class) {
                int[] ints = (int[]) values;
                valuesArray = new Value[ints.length];
                for (int i = 0; i < ints.length; i++) {
                    valuesArray[i] = createValue(int.class, ints[i], valueFactory);
                }
            } else if (type.getComponentType() == long.class) {
                long[] longs = (long[]) values;
                valuesArray = new Value[longs.length];
                for (int i = 0; i < longs.length; i++) {
                    valuesArray[i] = createValue(long.class, longs[i], valueFactory);
                }
            } else if (type.getComponentType() == double.class) {
                double[] doubles = (double[]) values;
                valuesArray = new Value[doubles.length];
                for (int i = 0; i < doubles.length; i++) {
                    valuesArray[i] = createValue(double.class, doubles[i], valueFactory);
                }
            } else if (type.getComponentType() == boolean.class) {
                boolean[] booleans = (boolean[]) values;
                valuesArray = new Value[booleans.length];
                for (int i = 0; i < booleans.length; i++) {
                    valuesArray[i] = createValue(boolean.class, booleans[i], valueFactory);
                }
            } else if (type.getComponentType() == Locale.class) {
                Locale[] locales = (Locale[]) values;
                valuesArray = new Value[locales.length];
                for (int i = 0; i < locales.length; i++) {
                    valuesArray[i] = createValue(Locale.class, locales[i], valueFactory);
                }
            } else {
                // Object
                Object[] objects = (Object[]) values;
                valuesArray = new Value[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    valuesArray[i] = createValue(type.getComponentType(), objects[i], valueFactory);
                }
            }
            nodeValues = valuesArray;
        }

        return nodeValues;
    }

    @Override
    public Object getObject(Field field, Object obj) throws IllegalAccessException {
        return field.get(obj);
    }

    @Override
    public void setObject(Field field, Object source, Object value) throws IllegalAccessException {
        field.set(source, value);
    }

}
