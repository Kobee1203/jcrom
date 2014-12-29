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

import static org.jcrom.util.ReflectionUtils.getMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.jcrom.annotations.JcrNode;
import org.jcrom.util.ReflectionUtils;

/**
 * Implementation of {@link TypeHandler} to handle JavaFX types.
 * 
 * @author Nicolas Dos Santos
 */
public class JavaFXTypeHandler extends DefaultTypeHandler {

    @Override
    public Object resolveAddEntity(Object entity) {
        if (Property.class.isAssignableFrom(entity.getClass())) {
            entity = ((Property) entity).getValue();
        }
        return super.resolveAddEntity(entity);
    }

    @Override
    public JcrNode getJcrNodeAnnotation(Class<?> type, Type genericType, Object source) {
        JcrNode fileJcrNode = null;
        if (ObjectProperty.class.isAssignableFrom(type)) {
            fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(ReflectionUtils.getObjectPropertyGeneric(source, type, genericType));
        } else {
            fileJcrNode = super.getJcrNodeAnnotation(type, genericType, source);
        }
        return fileJcrNode;
    }

    @Override
    public boolean isPropertyType(Class<?> type) {
        return super.isPropertyType(type);
    }

    @Override
    public boolean isValidMapValueType(Class<?> type) {
        return super.isValidMapValueType(type) || type == StringProperty.class || type == IntegerProperty.class || type == DoubleProperty.class || type == LongProperty.class || type == BooleanProperty.class || type == ObjectProperty.class;
    }

    @Override
    public boolean isDateType(Class<?> type) {
        return super.isDateType(type);
    }

    @Override
    public boolean isMap(Class<?> type) {
        return ObservableMap.class.isAssignableFrom(type) || super.isMap(type);
    }

    @Override
    public boolean isList(Class<?> type) {
        return ObservableList.class.isAssignableFrom(type) || super.isList(type);
    }

    @Override
    public boolean isString(Class<?> type) {
        return type == StringProperty.class || super.isString(type);
    }

    @Override
    public Class<?> getType(Class<?> type, Type genericType, Object source) {
        Class<?> t = type;
        if (ObjectProperty.class.isAssignableFrom(type)) {
            t = ReflectionUtils.getObjectPropertyGeneric(source, type, genericType);
        }
        return super.getType(t, genericType, source);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, Value value, Object currentObj) throws RepositoryException {
        Object fieldValue;

        if (ObjectProperty.class.isAssignableFrom(type)) {
            fieldValue = getValue(ReflectionUtils.getObjectPropertyGeneric(null, type, genericType), genericType, value, currentObj);
        } else if (StringProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getString();
        } else if (IntegerProperty.class.isAssignableFrom(type)) {
            fieldValue = (int) value.getDouble();
        } else if (LongProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getLong();
        } else if (DoubleProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getDouble();
        } else if (BooleanProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getBoolean();
        } else {
            fieldValue = super.getValue(type, genericType, value, currentObj);
        }

        return fieldValue;
    }

    @Override
    public Object getValues(Class<?> type, Type genericType, Value[] values, Object currentObj) throws RepositoryException {
        Object fieldValue;

        if (ObservableList.class.isAssignableFrom(type)) {
            ObservableList<Object> list = (ObservableList) currentObj;
            List<Object> properties = new ArrayList<Object>();
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
            for (Value v : values) {
                properties.add(getValue(paramClass, null, v, null));
            }
            list.setAll(properties);
            fieldValue = list;
        } else {
            fieldValue = super.getValues(type, genericType, values, currentObj);
        }

        return fieldValue;
    }

    @Override
    public Value createValue(Class<?> c, Object value, ValueFactory valueFactory) throws RepositoryException {
        Value nodeValue = null;

        if (Property.class.isAssignableFrom(c)) {
            Object wrappedValue = ((Property) value).getValue();
            if (wrappedValue != null) {
                nodeValue = createValue(wrappedValue.getClass(), wrappedValue, valueFactory);
            }
        } else {
            nodeValue = super.createValue(c, value, valueFactory);
        }

        return nodeValue;
    }

    @Override
    public Value[] createValues(Class<?> type, Class<?> genericType, Object values, ValueFactory valueFactory) throws RepositoryException {
        Value[] nodeValues;

        if (ObservableList.class.isAssignableFrom(type)) {
            // multi-value property List
            ObservableList<?> fieldValues = (ObservableList<?>) values;
            if (!fieldValues.isEmpty()) {
                Value[] valuesArray = new Value[fieldValues.size()];
                for (int i = 0; i < fieldValues.size(); i++) {
                    valuesArray[i] = createValue(genericType, fieldValues.get(i), valueFactory);
                }
                nodeValues = valuesArray;
            } else {
                nodeValues = new Value[0];
            }
        } else {
            nodeValues = super.createValues(type, genericType, values, valueFactory);
        }

        return nodeValues;
    }

    @Override
    public Object getObject(Field field, Object obj) throws IllegalAccessException {
        if (Property.class.isAssignableFrom(field.getType())) {
            return getPropertyValue(field, obj);
        } else {
            return super.getObject(field, obj);
        }
    }

    @Override
    public void setObject(Field field, Object source, Object value) throws IllegalAccessException {
        if (value == null) {
            return;
        }
        if (ObservableMap.class.isAssignableFrom(field.getType())) {
            Object mapProperty = field.get(source);
            if (mapProperty != null) {
                ((ObservableMap) mapProperty).putAll((Map) value);
            } else {
                try {
                    ((ObservableMap) getPropertyByPropertyGetter(field, source)).putAll((Map) value);
                } catch (NoSuchMethodException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } else if (ObservableList.class.isAssignableFrom(field.getType())) {
            Collection valueCollection = null;
            Collection valueColl = (Collection) value;
            try {
                // Create a new instance typified with the class of 'value'
                valueCollection = valueColl.getClass().getConstructor().newInstance();
                valueCollection.addAll(valueColl);
            } catch (Exception e) {
                // Default constructor not found, try to find a constructor with one parameter which extends Collection
                for (Constructor<?> constr : valueColl.getClass().getConstructors()) {
                    Class<?>[] parameterTypes = constr.getParameterTypes();
                    if (parameterTypes != null && parameterTypes.length == 1 && Collection.class.isAssignableFrom(parameterTypes[0])) {
                        try {
                            valueCollection = (Collection) constr.newInstance(FXCollections.observableArrayList(valueColl));
                            break;
                        } catch (Exception e1) {
                        }
                    }
                }
            }
            valueCollection = valueCollection != null ? valueCollection : (Collection) value;

            Object list = field.get(source);
            if (list != null) {
                ((ObservableList) list).setAll(valueCollection);
            } else {
                try {
                    ((ObservableList) getPropertyByPropertyGetter(field, source)).setAll(valueCollection);
                } catch (NoSuchMethodException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } else if (Property.class.isAssignableFrom(field.getType())) {
            setPropertyValue(field, source, value);
        } else {
            super.setObject(field, source, value);
        }
    }

    private static Property getPropertyByPropertyGetter(Field field, Object obj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Property property;
        Method propertyGetter = getMethod(obj.getClass(), field.getName() + "Property");
        property = (Property) propertyGetter.invoke(obj);
        return property;
    }

    /**
     * This method tries to access to the value of a JavaFX property field using the following strategy:
     * it first tries to read directly the property value (e.g. name.getValue()). If the property is null
     * we try the retrieve the value getter for this field (e.g. getName) to read the value. If the getter
     * does not exist, we try to retrieve the property getter (e.g. nameProperty()) to access the property
     * and then to get its value.
     */
    private static Object getPropertyValue(Field field, Object obj) throws IllegalAccessException {
        Property property = (Property) field.get(obj);
        if (property != null) {
            return property.getValue();
        } else {
            try {
                Method getter = getMethod(obj.getClass(), "get" + capitalize(field.getName()));
                return getter.invoke(obj);
            } catch (NoSuchMethodException e) {
                try {
                    property = getPropertyByPropertyGetter(field, obj);
                    return property.getValue();
                } catch (NoSuchMethodException e1) {
                    return null;
                } catch (InvocationTargetException e1) {
                    return null;
                }
            } catch (InvocationTargetException e) {
                return null;
            }
        }
    }

    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Invoke the setter of a property in the current thread and then in the JavaFX thread.
     * Actually, if a visible node of the JavaFX scene graph is bound to the property, it
     * will be only possible to update its value in the JavaFX thread.
     */
    private static void invokeSetter(final Object obj, final Object value, final Method setter) {
        try {
            setter.invoke(obj, value);
        } catch (RuntimeException e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        setter.invoke(obj, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the value of a JavaFX property in the current thread and then in the JavaFX thread.
     * Actually, if a visible node of the JavaFX scene graph is bound to the property, it
     * will be only possible to update its value in the JavaFX thread.
     * <p/>
     * If the property is bound, this method does nothing.
     */
    private static void updateProperty(final Object value, final Property finalProperty) {
        if (finalProperty.isBound()) {
            System.out.println("Impossible to set the value " + value + " to " + finalProperty.getName() + ". Property is bound.");
            return;
        }

        try {
            finalProperty.setValue(value);
        } catch (RuntimeException e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    finalProperty.setValue(value);
                }
            });
        }
    }

    /**
     * Set the value of a JavaFX property field by applying the following strategy:
     * If the field is not null, the value is set directly on the property (e.g. name.setValue("")).
     * If the property is null, i.e. it has not been initialized, we try to retrieve the value setter for
     * this field (e.g. setName()) to set the value. If the setter
     * does not exist, we try to retrieve the property getter (e.g. nameProperty()) to access the property
     * and then to set its value.
     */
    private static void setPropertyValue(Field field, final Object obj, final Object value) throws IllegalAccessException {
        Property property = (Property) field.get(obj);
        if (property != null) {
            updateProperty(value, property);
        } else {
            try {
                final Method setter = getMethod(obj.getClass(), "set" + capitalize(field.getName()), value.getClass());
                invokeSetter(obj, value, setter);
            } catch (NoSuchMethodException e) {
                try {
                    property = getPropertyByPropertyGetter(field, obj);
                    updateProperty(value, property);
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
