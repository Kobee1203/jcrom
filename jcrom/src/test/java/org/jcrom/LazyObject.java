package org.jcrom;

import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class LazyObject extends AbstractJcrEntity implements LazyInterface {

	@JcrProperty private String s;
	
	public LazyObject() {
		super();
	}
	
	public void setString( String s ) {
		this.s = s;
	}
	
	public String getString() {
		return s;
	}
}
