/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(
	mixinTypes= {"mix:referenceable"}
)
public class Parent extends AbstractEntity implements Serializable {
	
	@JcrUUID String uuid;
	
	@JcrProperty List<String> tags;
	
	@JcrChildNode
	private Child adoptedChild;
	
	@JcrChildNode
	private List<Child> children;
	
	@JcrFileNode
	private Photo passportPhoto;
	
	@JcrFileNode(loadType = JcrFileNode.LoadType.BYTES)
	private List<JcrFile> files;
	
	@JcrReference private ReferencedEntity reference;
	
	public Parent() {
		tags = new ArrayList<String>();
		children = new ArrayList<Child>();
		files = new ArrayList<JcrFile>();
	}

	public ReferencedEntity getReference() {
		return reference;
	}

	public void setReference(ReferencedEntity reference) {
		this.reference = reference;
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
	
	public void addChild( Child child ) {
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
	
	public void addFile( JcrFile file ) {
		files.add(file);
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public void addTag( String tag ) {
		tags.add(tag);
	}
}
