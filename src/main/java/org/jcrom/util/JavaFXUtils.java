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
package org.jcrom.util;

import static org.jcrom.util.ReflectionUtils.getMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 05/08/2014
 * Time: 21:53
 */
public final class JavaFXUtils {

    private JavaFXUtils() {
    }

    /**
     * Get the value of the field represented by this {@code Field}, on
     * the specified object. If the field is a JavaFX property, the value
     * of the property and not the property itself is automatically returned.
     */
    public static Object getObject(Field field, Object obj) throws IllegalAccessException {
        if (javafx.beans.property.Property.class.isAssignableFrom(field.getType())) {
            return getPropertyValue(field, obj);
        } else {
            return field.get(obj);
        }
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

    private static Property getPropertyByPropertyGetter(Field field, Object obj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Property property;
        Method propertyGetter = getMethod(obj.getClass(), field.getName() + "Property");
        property = (Property) propertyGetter.invoke(obj);
        return property;
    }

    /**
     * Sets the field represented by this {@code Field} object on the
     * specified object argument to the specified new value. If the field
     * is a JavaFX Property, then the value of the property will be set and not
     * the property itself.
     */
    public static void setObject(Field field, Object source, Object value) throws IllegalAccessException {
        if (value == null) {
            return;
        }
        if (MapProperty.class.isAssignableFrom(field.getType())) {
            Object mapProperty = field.get(source);
            if (mapProperty != null) {
                ((MapProperty) mapProperty).putAll((Map) value);
            } else {
                try {
                    ((MapProperty) getPropertyByPropertyGetter(field, source)).putAll((Map) value);
                } catch (NoSuchMethodException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } else if (ListProperty.class.isAssignableFrom(field.getType())) {
            Object listProperty = field.get(source);
            if (listProperty != null) {
                ((ListProperty) listProperty).setAll((Collection) value);
            } else {
                try {
                    ((ListProperty) getPropertyByPropertyGetter(field, source)).setAll((Collection) value);
                } catch (NoSuchMethodException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } else if (javafx.beans.property.Property.class.isAssignableFrom(field.getType())) {
            setPropertyValue(field, source, value);
        } else {
            field.set(source, value);
        }
    }

    public static boolean isMap(Class<?> clazz) {
        return ReflectionUtils.implementsInterface(clazz, Map.class) || MapProperty.class.isAssignableFrom(clazz);
    }

    public static boolean isList(Class<?> clazz) {
        return ReflectionUtils.implementsInterface(clazz, List.class) || ListProperty.class.isAssignableFrom(clazz);
    }

    public static boolean isNotString(Field field) {
        return field.getType() != String.class && field.getType() != StringProperty.class;
    }

    /**
     * Returns the type of a field. If the field is an ObjectProperty,
     * then the parametrized type of this ObjectProperty is returned, i.e.
     * the type of its value.
     */
    public static Class<?> getType(Field field, Object source) {
        if (ObjectProperty.class.isAssignableFrom(field.getType())) {
            return ReflectionUtils.getObjectPropertyGeneric(source, field);
        } else {
            return field.getType();
        }
    }

}
