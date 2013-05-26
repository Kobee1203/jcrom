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
package org.jcrom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class VersionedEntity extends AbstractJcrEntity implements VersionedInterface {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private String title;
    @JcrProperty
    private String body;
    @JcrBaseVersionName
    private String baseVersionName;
    @JcrBaseVersionCreated
    private Date baseVersionCreated;
    @JcrVersionName
    private String versionName;
    @JcrVersionCreated
    private Date versionCreated;
    @JcrCheckedout
    private boolean checkedOut;
    @JcrChildNode(containerMixinTypes = { "mix:versionable" })
    List<VersionedEntity> versionedChildren;
    @JcrChildNode
    List<Child> unversionedChildren;
    @JcrChildNode(createContainerNode = true, containerMixinTypes = { "mix:versionable" })
    VersionedEntity versionedChild1;
    @JcrChildNode(createContainerNode = true)
    VersionedEntity versionedChild2;
    @JcrChildNode(createContainerNode = false, containerMixinTypes = { "mix:versionable" })
    VersionedEntity versionedChild3;
    @JcrChildNode(createContainerNode = false)
    VersionedEntity versionedChild4;

    public VersionedEntity() {
        this.versionedChildren = new ArrayList<VersionedEntity>();
        this.unversionedChildren = new ArrayList<Child>();
    }

    public Date getBaseVersionCreated() {
        return baseVersionCreated;
    }

    public void setBaseVersionCreated(Date baseVersionCreated) {
        this.baseVersionCreated = baseVersionCreated;
    }

    public String getBaseVersionName() {
        return baseVersionName;
    }

    public void setBaseVersionName(String baseVersionName) {
        this.baseVersionName = baseVersionName;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.name = title;
    }

    public Date getVersionCreated() {
        return versionCreated;
    }

    public void setVersionCreated(Date versionCreated) {
        this.versionCreated = versionCreated;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Child> getUnversionedChildren() {
        return unversionedChildren;
    }

    public void setUnversionedChildren(List<Child> unversionedChildren) {
        this.unversionedChildren = unversionedChildren;
    }

    public void addUnversionedChild(Child child) {
        unversionedChildren.add(child);
    }

    public List<VersionedEntity> getVersionedChildren() {
        return versionedChildren;
    }

    public void setVersionedChildren(List<VersionedEntity> versionedChildren) {
        this.versionedChildren = versionedChildren;
    }

    public void addVersionedChild(VersionedEntity child) {
        versionedChildren.add(child);
    }
}
