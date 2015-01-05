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

import org.jcrom.JcrFile;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 * Models a Document, contains a file.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(nodeType = "nt:unstructured", classNameProperty = "className", mixinTypes = { "mix:referenceable" })
public class Document extends HierarchyNode {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    private String uuid;

    @JcrFileNode
    private JcrFile file;

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
     * @return the file
     */
    public final JcrFile getFile() {
        return file;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param file the file to set
     */
    public final void setFile(JcrFile file) {
        this.file = file;
    }

}
