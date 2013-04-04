package org.jcrom;

import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrReference;

public class JcrFileReferenceParentNode extends AbstractJcrEntity {

	private static final long serialVersionUID = -6552547713989381887L;

	@JcrReference
	@JcrFileNode
	private ReferenceableJCRFile bRef;

	public JcrFileReferenceParentNode() {
		super();
	}

	public ReferenceableJCRFile getbRef() {
		return bRef;
	}

	public void setbRef(ReferenceableJCRFile bRef) {
		this.bRef = bRef;
	}

}