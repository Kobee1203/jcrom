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

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.jcrom.annotations.JcrNode;

/**
 * Interface to handle types. Default implementation is {@link DefaultTypeHandler}.
 * 
 * @author Nicolas Dos Santos
 */
public interface TypeHandler {

    /**
     * Resolve the given entity before adding the related node in JCRF repository.
     * 
     * @param entity entity
     * @return resolved entity
     */
    Object resolveAddEntity(Object entity);

    /**
     * Returns the {@link JcrNode} annotation of the specified class.
     * 
     * @param type the class we want to check
     * @param genericType generic type
     * @param entity entity
     * @return
     */
    JcrNode getJcrNodeAnnotation(Class<?> type, Type genericType, Object entity);

    /**
     * Check if the class supplied represents a valid JCR property type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a valid JCR property type
     */
    boolean isPropertyType(Class<?> type);

    /**
     * Check if the class supplied represents a valid Map value type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a valid Map value type
     */
    boolean isValidMapValueType(Class<?> type);

    /**
     * Check if the class supplied represents a valid date type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a valid date type
     */
    boolean isDateType(Class<?> type);

    /**
     * Check if the class supplied represents a Map type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a Map type
     */
    boolean isMap(Class<?> type);

    /**
     * Check if the class supplied represents a List type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a List type
     */
    boolean isList(Class<?> type);

    /**
     * Check if the class supplied represents a String type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a String type
     */
    boolean isString(Class<?> type);

    /**
     * Returns the class represented by the given type.
     * 
     * @param type type
     * @param genericType generic type
     * @param source source object
     * @return {@link Class}
     */
    Class<?> getType(Class<?> type, Type genericType, Object source);

    /**
     * Returns a representation of the given {@link Value} by using the given {@link Class}.
     * 
     * @param type {@link Class} used to return the representation with correct type
     * @param genericType generic type used to return the representation with correct type
     * @param value {@link Value} object
     * @param currentObj current object
     * @return a representation of {@link Value}.
     * @throws RepositoryException
     */
    Object getValue(Class<?> type, Type genericType, Value value, Object currentObj) throws RepositoryException;

    /**
     * Returns a representation of the given {@link Value}s by using the given {@link Class}.
     * 
     * @param type {@link Class} used to return the representation with correct type
     * @param genericType generic type used to return the representation with correct type
     * @param values array of {@link Value} object
     * @param currentObj current object
     * @return a representation of {@link Value}s.
     * @throws RepositoryException
     */
    Object getValues(Class<?> type, Type genericType, Value[] values, Object currentObj) throws RepositoryException;

    /**
     * Returns a {@link Value} object from the given class and the specified value.
     * 
     * @param c {@link Class} used to create the {@link Value} object with the correct {@link javax.jcr.PropertyType}
     * @param value the value
     * @param valueFactory {@link ValueFactory} used to invoke the methods to create {@link Value}
     * @return a {@link Value} object
     * @throws RepositoryException
     */
    Value createValue(Class<?> c, Object value, ValueFactory valueFactory) throws RepositoryException;

    /**
     * Returns an array of {@link Value} objects from the given class and the specified values.
     * 
     * @param type {@link Class} used to create the array of {@link Value} objects with the correct {@link javax.jcr.PropertyType}
     * @param genericType generic type used to create the array of {@link Value} objects with the correct {@link javax.jcr.PropertyType}
     * @param value the values
     * @param valueFactory {@link ValueFactory} used to invoke the methods to create {@link Value}
     * @return an array of {@link Value} objects
     * @throws RepositoryException
     */
    Value[] createValues(Class<?> type, Class<?> genericType, Object values, ValueFactory valueFactory) throws RepositoryException;

    /**
     * Returns the value of the given field present in the given object.
     * 
     * @param field field
     * @param obj object
     * @return object representing the value of the field
     * @throws IllegalAccessException
     */
    Object getObject(Field field, Object obj) throws IllegalAccessException;

    /**
     * Set the given value to the given field present in the given source object.
     * 
     * @param field field
     * @param source source object
     * @param value value to set
     * @throws IllegalAccessException
     */
    void setObject(Field field, Object source, Object value) throws IllegalAccessException;

}
