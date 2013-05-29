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
package org.jcrom;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 * Models a folder.
 */
@JcrNode(nodeType = NodeType.NT_UNSTRUCTURED, classNameProperty = "className", mixinTypes = { NodeType.MIX_REFERENCEABLE })
public class Folder extends HierarchyNode {

    private static final long serialVersionUID = 1L;

    @JcrIdentifier
    private String id;
    @JcrUUID
    private String uuid;

    @JcrChildNode(containerNodeType = "nt:unstructured")
    private List<HierarchyNode> children = new ArrayList<HierarchyNode>();

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the uuid
     */
    public final String getUuid() {
        return uuid;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param uuid the uuid to set
     */
    public final void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the children
     */
    public final List<HierarchyNode> getChildren() {
        return children;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param children the children to set
     */
    public final void setChildren(List<HierarchyNode> children) {
        this.children = children;
    }

}
