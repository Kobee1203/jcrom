package org.jcrom;

import org.jcrom.annotations.JcrNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(classNameProperty="className")
public interface A {

    public void setB( B b );

    public B getB();
}
