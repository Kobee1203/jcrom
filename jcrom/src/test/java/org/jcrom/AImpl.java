package org.jcrom;

import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class AImpl extends AbstractJcrEntity implements A {

    @JcrChildNode private B b;

    public AImpl() {
        this(null);
    }

    public AImpl( String name ) {
        super();
        this.name = name;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}
