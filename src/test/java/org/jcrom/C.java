package org.jcrom;

import org.jcrom.annotations.JcrNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(classNameProperty="className")
public interface C {

    public void setA( A a );
    public A getA();
}
