package org.jcrom;

import org.jcrom.annotations.JcrNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(classNameProperty="className")
public interface B {

    public void setC( C c );
    public C getC();
}
