package org.jcrom;

import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class LazyObject extends AbstractJcrEntity implements LazyInterface {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private String s;

    public LazyObject() {
        super();
    }

    @Override
    public void setString(String s) {
        this.s = s;
    }

    @Override
    public String getString() {
        return s;
    }
}
