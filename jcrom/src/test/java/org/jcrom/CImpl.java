package org.jcrom;

import org.jcrom.annotations.JcrReference;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class CImpl extends AbstractJcrEntity implements C {

    @JcrReference(byPath=true) private A body;

    public CImpl() {
        this(null);
    }

    public CImpl( String name ) {
        super();
        this.name = name;
    }

    public A getA() {
        return body;
    }

    public void setA(A body) {
        this.body = body;
    }


}
