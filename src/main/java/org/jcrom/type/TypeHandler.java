/*
 * This file is part of the Weedow jcrom (R) project.
 * Copyright (c) 2010-2014 Weedow Software Corp.
 * Authors: Nicolas Dos Santos
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY Weedow, 
 * Weedow DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://www.weedow.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving this program without disclosing
 * the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP, 
 * serving this program in a web application, shipping this program with a closed
 * source product.
 *
 * For more information, please contact Weedow Software Corp. at this
 * address: nicolas.dossantos@gmail.com
 */
package org.jcrom.type;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.jcrom.annotations.JcrNode;

/**
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
     * 
     * @param entity
     * @param field
     * @return
     */
    JcrNode getJcrAnnotation(Object entity, Class<?> type, Type genericType);

    /**
     * Check if the class supplied represents a valid JCR property type.
     * 
     * @param type the class we want to check
     * @return true if the class represents a valid JCR property type
     */
    boolean isPropertyType(Class<?> type);

    boolean isValidMapValueType(Class<?> type);

    boolean isDateType(Class<?> type);

    boolean isMap(Class<?> type);

    boolean isList(Class<?> type);

    boolean isString(Class<?> type);

    Class<?> getType(Class<?> type, Type genericType, Object source);

    Object getValue(Class<?> type, Type genericType, Value value, Object currentObj) throws RepositoryException;

    Object getValues(Class<?> type, Type genericType, Value[] values, Object currentObj) throws RepositoryException;

    Value createValue(Class<?> c, Object value, ValueFactory valueFactory) throws RepositoryException;

    Value[] createValues(Class<?> c, Class<?> paramClass, Object values, ValueFactory valueFactory) throws RepositoryException;

    Object getObject(Field field, Object obj) throws IllegalAccessException;

    void setObject(Field field, Object source, Object value) throws IllegalAccessException;

}
