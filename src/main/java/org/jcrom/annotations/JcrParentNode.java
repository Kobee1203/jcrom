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
package org.jcrom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is added to fields that implement JcrEntity, and are
 * a reference to a parent object. Such fields are ignored when mapping to
 * a JCR node, but when loading an object from a node, then field annotated
 * with @JcrParentNode will be populated with the mapped parent object (if
 * it was mapped).
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JcrParentNode {

}
