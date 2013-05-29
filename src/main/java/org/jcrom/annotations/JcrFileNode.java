/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2013 - All rights reserved.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This annotation is used to mark fields that are to be mapped as
 * JCR file nodes.
 * It can be applied to fields whos type is a JcrFile (or extension of that), or
 * to a java.util.List that is parameterized with a JcrFile (or extension).
 * <br/><br/>
 * Note that JCROM creates a container node for all file nodes. The file
 * node is then created with the name retrieved via calling JcrEntity.getName()
 * on the child object.
 * 
 * @author Olafur Gauti Gudmundsson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JcrFileNode {

    public enum LoadType {

        STREAM, BYTES
    }

    /**
     * The name of the JCR container node for the file node(s).
     * Defaults to the name of the field being annotated.
     *
     * @return the name of the JCR node storing the child that the field
     * represents
     */
    String name() default "fieldName";

    /**
     * Determines how to read the file when JCROM maps a JCR node to an object.
     * Defaults to LoadType.STREAM, but can also be set to LoadType.BYTES (in
     * which case the content will be read into a byte array).
     *
     * @return a value determining how to load the File from JCR
     */
    LoadType loadType() default LoadType.STREAM;

    /**
     * Setting this to true will turn on lazy loading for this field.
     * The default is false.
     *
     * @return whether to apply lazy loading to this field
     */
    boolean lazy() default false;

    /**
     * Specify the class of the file list child node container.
     *
     * @return The class of the file list container
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
