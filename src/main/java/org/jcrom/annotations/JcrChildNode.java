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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.nodetype.NodeType;

/**
 * This annotation is used to mark fields that are to be mapped as
 * JCR child nodes.
 * It can be applied to fields whos type has been mapped to Jcrom, or
 * to a java.util.List that is parameterized with a mapped implementation.
 * <br/><br/>
 * Note that JCROM creates a container node for all child nodes. The child
 * node is then created with the name retrieved via calling JcrEntity.getName()
 * on the child object.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
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
     * Defaults to "nt:unstructured" ( {@link javax.jcr.nodetype.NodeType#NT_UNSTRUCTURED} ).
     *
     * @return the node type to use when creating a container node for
     * the children
     */
    String containerNodeType() default NodeType.NT_UNSTRUCTURED;

    /**
     * Mixin types to be added to the container node.
     *
     * @return the mixin types for the child container node
     */
    String[] containerMixinTypes() default {};

    /**
     * Setting this to true will turn on lazy loading for this field.
     * The default is false.
     *
     * @return whether to apply lazy loading to this field
     */
    boolean lazy() default false;

    /**
     * Setting this to false will mean not creating a container node
     * for single child objects. This does not apply for Lists or Maps of
     * child nodes.
     * The default is true.
     * 
     * @return whether to create a child container node
     */
    boolean createContainerNode() default true;

    /**
     * Specify the class of the list child node container.
     *
     * @return The class of the list container
     * @since 1.4
     */
    Class<? extends List> listContainerClass() default ArrayList.class;

    /**
     * Specify the class of the map child node container.
     *
     * @return The class of the map container
     * @since 1.4
     */
    Class<? extends Map> mapContainerClass() default HashMap.class;
}
