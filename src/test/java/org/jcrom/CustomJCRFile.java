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

import javax.jcr.nodetype.NodeType;

import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrUUID;

/**
 * Thanks to Danilo Barboza and Leander for contributing this test class.
 */
@JcrNode(nodeType = NodeType.NT_UNSTRUCTURED, mixinTypes = { NodeType.MIX_REFERENCEABLE })
public class CustomJCRFile extends JcrFile {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    private String uuid;

    @JcrProperty
    private String metadata;

    public CustomJCRFile() {
        super();
    }

    /**
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * @param uuid
     *            the uuid to set
     */
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
