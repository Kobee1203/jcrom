/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(mixinTypes = { "mix:referenceable" }, classNameProperty = "className")
public class TreeNode extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    private String uuid;
    @JcrChildNode(lazy = true)
    private List<TreeNode> children;

    public TreeNode() {
        this(null);
    }

    public TreeNode(String name) {
        super();
        this.name = name;
        this.children = new ArrayList<TreeNode>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }
}
