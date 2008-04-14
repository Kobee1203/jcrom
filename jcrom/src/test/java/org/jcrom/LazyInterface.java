package org.jcrom;

import org.jcrom.annotations.JcrNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(classNameProperty="className")
public interface LazyInterface extends JcrEntity {

	public void setString( String s );
	
	public String getString();
}
