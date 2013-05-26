/**
 * Copyright (C) 2008-2013 by JCROM Authors (Olafur Gauti Gudmundsson, Nicolas Dos Santos) - All rights reserved.
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
package org.jcrom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark fields that are to be mapped as JCR reference properties.
 * 
 * @author Olafur Gauti Gudmundsson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface JcrReference {

    /**
     * The name of the JCR reference property. Defaults to the name of the field being annotated.
     * 
     * @return the name of the JCR reference property
     */
    String name() default "fieldName";

    /**
     * Setting this to true will turn on lazy loading for this field. The default is false.
     * 
     * @return whether to apply lazy loading to this field
     */
    boolean lazy() default false;

    /**
     * Setting this to true will store the reference as the path to the referenced entity in a normal String property,
     * rather than a reference property.
     * 
     * @return whether to use path-based references
     */
    boolean byPath() default false;

    /**
     * Setting this to true will force a weak reference for this field. The default is false.
     * 
     * @return whether to force this field to use a weak reference
     */
    boolean weak() default false;
}
