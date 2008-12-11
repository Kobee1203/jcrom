package org.jcrom;

import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class BImpl extends AbstractJcrEntity implements B {

    @JcrChildNode private C c;

    public BImpl() {
        this(null);
    }

    public BImpl( String name ) {
        super();
        this.name = name;
    }

    public C getC() {
        return c;
    }

    public void setC(C c) {
        this.c = c;
    }

}
