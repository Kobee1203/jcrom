package org.jcrom;

import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class AImpl extends AbstractJcrEntity implements A {

    private static final long serialVersionUID = 1L;

    @JcrChildNode
    private B b;

    public AImpl() {
        this(null);
    }

    public AImpl(String name) {
        super();
        this.name = name;
    }

    @Override
    public B getB() {
        return b;
    }

    @Override
    public void setB(B b) {
        this.b = b;
    }
}
