package org.jcrom;

import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrReference;

public class JcrFileReferenceByPathParentNode extends AbstractJcrEntity {

	private static final long serialVersionUID = -6552547713989381887L;

	@JcrReference(byPath=true)
	@JcrFileNode
	private ReferenceableJCRFile bRef;

	public JcrFileReferenceByPathParentNode() {
		super();
	}

	public ReferenceableJCRFile getbRef() {
		return bRef;
	}

	public void setbRef(ReferenceableJCRFile bRef) {
		this.bRef = bRef;
	}

}