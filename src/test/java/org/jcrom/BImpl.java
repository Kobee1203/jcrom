package org.jcrom;

import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class BImpl extends AbstractJcrEntity implements B {

    private static final long serialVersionUID = 1L;

    @JcrChildNode
    private C c;

    public BImpl() {
        this(null);
    }

    public BImpl(String name) {
        super();
        this.name = name;
    }

    @Override
    public C getC() {
        return c;
    }

    @Override
    public void setC(C c) {
        this.c = c;
    }

}
