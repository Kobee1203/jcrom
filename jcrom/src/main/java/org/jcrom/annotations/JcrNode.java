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
package org.jcrom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applied to types (classes or interfaces) that implement
 * JcrEntity, and provides the ability to specify what JCR node type to use
 * when creating a JCR node from the object being mapped.
 * 
 * @author Olafur Gauti Gudmundsson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JcrNode {

	/**
	 * The node type to be applied for this JCR object.
	 * Defaults to "nt:unstructured".
	 * 
	 * @return the node type to use when creating a JCR node for this object
	 */
	String nodeType() default "nt:unstructured";
	
	/**
	 * Name of a JCR property to store the full name of the class being mapped.
	 * Value of "none" will lead to the class name not being stored.
	 * 
	 * @return name of a property to store the class name in
	 */
	String classNameProperty() default "none";
}
