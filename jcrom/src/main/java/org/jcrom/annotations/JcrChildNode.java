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
 * This annotation is used to mark fields that are to be mapped as
 * JCR child nodes.
 * It can be applied to fields whos type is a JcrEntity implementation, or
 * to a java.util.List that is parameterized with a JcrEntity implementation.
 * <br/><br/>
 * Note that JCROM creates a container node for all child nodes. The child
 * node is then created with the name retrieved via calling JcrEntity.getName()
 * on the child object.
 * 
 * @author Olafur Gauti Gudmundsson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JcrChildNode {
	
	/**
	 * The name of the JCR container node for the child/children. 
	 * Defaults to the name of the field being annotated.
	 * 
	 * @return the name of the JCR node storing the child that the field
	 * represents
	 */
	String name() default "fieldName";
	
	/**
	 * The node type to be applied for the child container node.
	 * Defaults to "nt:unstructured".
	 * 
	 * @return the node type to use when creating a container node for
	 * the children
	 */
	String containerNodeType() default "nt:unstructured";
}
