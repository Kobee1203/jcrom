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

import java.util.Date;

import javax.jcr.Property;
import javax.jcr.nodetype.NodeType;

import org.jcrom.annotations.JcrCreated;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProtectedProperty;

/**
 * 
 * @author Nicolas Dos Santos
 */
@JcrNode(nodeType = NodeType.NT_UNSTRUCTURED, mixinTypes = { NodeType.MIX_CREATED, NodeType.MIX_LOCKABLE, NodeType.MIX_REFERENCEABLE })
public class ProtectedPropertyNode extends AbstractEntity {

    @JcrCreated
    private Date created;

    @JcrIdentifier
    private String identifier;

    @JcrProtectedProperty(name = Property.JCR_UUID)
    private String id;

    @JcrProtectedProperty(name = Property.JCR_CREATED_BY)
    private String createdBy;

    @JcrProtectedProperty(name = Property.JCR_LOCK_OWNER)
    private String lockOwner;

    @JcrProtectedProperty(name = Property.JCR_LOCK_IS_DEEP)
    private boolean lockIsDeep;

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the lockOwner
     */
    public String getLockOwner() {
        return lockOwner;
    }

    /**
     * @param lockOwner the lockOwner to set
     */
    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    /**
     * @return the lockIsDeep
     */
    public boolean isLockIsDeep() {
        return lockIsDeep;
    }

    /**
     * @param lockIsDeep the lockIsDeep to set
     */
    public void setLockIsDeep(boolean lockIsDeep) {
        this.lockIsDeep = lockIsDeep;
    }
}
