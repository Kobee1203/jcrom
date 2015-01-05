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

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrParentNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class Child extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JcrParentNode
    private Parent parent;

    @JcrChildNode
    private GrandChild adoptedGrandChild;

    @JcrChildNode
    private List<GrandChild> grandChildren;

    public Child() {
        this.grandChildren = new ArrayList<GrandChild>();
    }

    public void addGrandChild(GrandChild grandChild) {
        grandChildren.add(grandChild);
    }

    public List<GrandChild> getGrandChildren() {
        return grandChildren;
    }

    public void setGrandChildren(List<GrandChild> grandChildren) {
        this.grandChildren = grandChildren;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public GrandChild getAdoptedGrandChild() {
        return adoptedGrandChild;
    }

    public void setAdoptedGrandChild(GrandChild adoptedGrandChild) {
        this.adoptedGrandChild = adoptedGrandChild;
    }
}
