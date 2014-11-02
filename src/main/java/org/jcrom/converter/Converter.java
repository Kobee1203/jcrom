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
package org.jcrom.converter;

import org.jcrom.annotations.JcrProperty;

/**
 * <p>
 * Interface to implement to customize the conversion from an entity attribute value to JCR property representation and conversely.<br/>
 * The class that implements this interface can then be added at the declaration of the annotation {@link JcrProperty}.<br/>
 * The parameter Y representing the JCR property type must be supported by Jcrom (see org.jcrom.Validator).
 * </p>
 * 
 * <p>Example:</p>
 * <pre>
 * public class ColorConverter implements Converter<Color, String> {
 *
 *   &#64;Override
 *   public String convertToJcrProperty(Color color) { ... }
 *
 *   &#64;Override
 *   public Color convertToEntityAttribute(String colorString) { ... }
 *
 * }
 * </pre>
 * <pre>
 * public class EntityWithConverter extends AbstractJcrEntity {
 *
 *   &#64;JcrProperty(converter = ColorConverter.class)
 *   private Color color;
 *   
 *   ...
 *   
 * }
 * </pre>
 * 
 * @param X the type of the entity attribute
 * @param Y the type of the JCR property
 * 
 * @author Nicolas Dos Santos
 */
public interface Converter<X, Y> {

    /**
     * Converts the value stored in the entity attribute into the data representation to be stored in the JCR property.
     * 
     * @param attribute the entity attribute value to be converted
     * @return the converted data to be stored in the JCR property
     */
    Y convertToJcrProperty(X attribute);

    /**
     * Converts the data stored in the JCR property into the value to be stored in the entity attribute.
     * 
     * @param jcrData the data from the JCR property to be converted
     * @return the converted value to be stored in the entity attribute
     */
    X convertToEntityAttribute(Y jcrData);

}
