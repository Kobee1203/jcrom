package org.jcrom;

import org.jcrom.annotations.JcrNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(classNameProperty="className")
public interface C {

    public void setBody( String body );
    public String getBody();
}
