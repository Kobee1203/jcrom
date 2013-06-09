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
package org.jcrom.entities;

import java.util.List;

import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrReference;

/**
 * Models a reference to a Folder.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(nodeType = "nt:unstructured", classNameProperty = "className")
public class FolderReference extends HierarchyNode {

    private static final long serialVersionUID = 1L;

    @JcrReference
    private Folder reference;

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the reference
     */
    public final Folder getReference() {
        return reference;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param reference the reference to set
     */
    public final void setReference(Folder reference) {
        this.reference = reference;
    }

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the children
     */
    public final List<HierarchyNode> getChildren() {
        return reference.getChildren();
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param children the children to set
     */
    public final void setChildren(List<HierarchyNode> children) {
        reference.setChildren(children);
    }
}
