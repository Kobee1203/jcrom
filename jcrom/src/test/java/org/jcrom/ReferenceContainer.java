package org.jcrom;

import java.util.ArrayList;
import java.util.List;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(
	mixinTypes= {"mix:referenceable"}
)
public class ReferenceContainer {

	@JcrName private String name;
	@JcrPath private String path;
	@JcrUUID private String uuid;
	
	@JcrReference private ReferencedEntity reference;
	@JcrReference private List<ReferencedEntity> references;

	public ReferenceContainer() {
		this.references = new ArrayList<ReferencedEntity>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public ReferencedEntity getReference() {
		return reference;
	}

	public void setReference(ReferencedEntity reference) {
		this.reference = reference;
	}

	public List<ReferencedEntity> getReferences() {
		return references;
	}

	public void setReferences(List<ReferencedEntity> references) {
		this.references = references;
	}
	
	public void addReference( ReferencedEntity ref ) {
		references.add(ref);
	}
}
