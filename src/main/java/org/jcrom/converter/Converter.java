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
