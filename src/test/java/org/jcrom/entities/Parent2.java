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
package org.jcrom.entities;

import java.io.Serializable;

import javax.jcr.nodetype.NodeType;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(mixinTypes = { NodeType.MIX_REFERENCEABLE })
public class Parent2 extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @JcrIdentifier
    String id;

    // Same name as the instance of the Child2 object name
    @JcrChildNode(createContainerNode = false, name = "Child21")
    private Child2 child;

    public Parent2() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Child2 getChild() {
        return child;
    }

    public void setChild(Child2 child) {
        this.child = child;
    }
}
