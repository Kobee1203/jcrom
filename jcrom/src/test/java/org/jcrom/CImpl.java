package org.jcrom;

import org.jcrom.annotations.JcrReference;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class CImpl extends AbstractJcrEntity implements C {

    private static final long serialVersionUID = 1L;

    @JcrReference(byPath = true)
    private A body;

    public CImpl() {
        this(null);
    }

    public CImpl(String name) {
        super();
        this.name = name;
    }

    @Override
    public A getA() {
        return body;
    }

    @Override
    public void setA(A body) {
        this.body = body;
    }

}
