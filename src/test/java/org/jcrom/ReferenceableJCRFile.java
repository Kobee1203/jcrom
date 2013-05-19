package org.jcrom;

import org.jcrom.annotations.JcrIdentifier;

public class ReferenceableJCRFile extends JcrFile {

	private static final long serialVersionUID = -683017819605245613L;

	@JcrIdentifier
	String id;

	public ReferenceableJCRFile() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}