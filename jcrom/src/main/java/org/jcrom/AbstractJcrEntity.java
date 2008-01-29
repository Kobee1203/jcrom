package org.jcrom;

/**
 * Abstract implementation of the JcrEntity. Has protected variables
 * for name and path, and implements the getters and setters.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public abstract class AbstractJcrEntity implements JcrEntity {

	protected String name;
	protected String path;
	
	public AbstractJcrEntity() {
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPath( String path ) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
