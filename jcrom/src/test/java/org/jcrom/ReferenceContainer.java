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
    
	@JcrReference(byPath=true) private ReferencedEntity referenceByPath;
	@JcrReference(byPath=true) private List<ReferencedEntity> referencesByPath;

	public ReferenceContainer() {
		this.references = new ArrayList<ReferencedEntity>();
        this.referencesByPath = new ArrayList<ReferencedEntity>();
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

    public ReferencedEntity getReferenceByPath() {
        return referenceByPath;
    }

    public void setReferenceByPath(ReferencedEntity referenceByPath) {
        this.referenceByPath = referenceByPath;
    }

    public List<ReferencedEntity> getReferencesByPath() {
        return referencesByPath;
    }

    public void setReferencesByPath(List<ReferencedEntity> referencesByPath) {
        this.referencesByPath = referencesByPath;
    }
    
    public void addReferenceByPath( ReferencedEntity reference ) {
        referencesByPath.add(reference);
    }
}
