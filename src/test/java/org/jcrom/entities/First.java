package org.jcrom.entities;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class First extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private String firstString;

    public First() {
        super();
    }

    public String getFirstString() {
        return firstString;
    }

    public void setFirstString(String firstString) {
        this.firstString = firstString;
    }
}
