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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jcrom.JcrFile;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(mixinTypes = { "mix:referenceable" }, classNameProperty = "className")
public class Parent extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @JcrIdentifier
    private String id;
    @JcrUUID
    private String uuid;
    @JcrProperty
    private List<String> tags;

    @JcrChildNode
    private Child adoptedChild;
    @JcrChildNode
    private List<Child> children;
    @JcrFileNode
    private Photo passportPhoto;
    @JcrFileNode
    private JcrFile jcrFile;
    @JcrFileNode(loadType = JcrFileNode.LoadType.BYTES)
    private List<JcrFile> files;

    @JcrChildNode(listContainerClass = LinkedList.class)
    private List<Child> customList;

    @JcrChildNode(mapContainerClass = TreeMap.class)
    private Map<String, Object> customMap;

    public Parent() {
        tags = new ArrayList<String>();
        children = new ArrayList<Child>();
        files = new ArrayList<JcrFile>();
        customList = new LinkedList<Child>();
        customMap = new TreeMap<String, Object>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Child getAdoptedChild() {
        return adoptedChild;
    }

    public void setAdoptedChild(Child adoptedChild) {
        this.adoptedChild = adoptedChild;
    }

    public void addChild(Child child) {
        children.add(child);
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public Photo getPassportPhoto() {
        return passportPhoto;
    }

    public void setPassportPhoto(Photo passportPhoto) {
        this.passportPhoto = passportPhoto;
    }

    public List<JcrFile> getFiles() {
        return files;
    }

    public void setFiles(List<JcrFile> files) {
        this.files = files;
    }

    public void addFile(JcrFile file) {
        files.add(file);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public JcrFile getJcrFile() {
        return jcrFile;
    }

    public void setJcrFile(JcrFile jcrFile) {
        this.jcrFile = jcrFile;
    }

    public List<Child> getCustomList() {
        return customList;
    }

    public void setCustomList(List<Child> customList) {
        this.customList = customList;
    }

    public Map<String, Object> getCustomMap() {
        return customMap;
    }

    public void setCustomMap(Map<String, Object> customMap) {
        this.customMap = customMap;
    }
}
