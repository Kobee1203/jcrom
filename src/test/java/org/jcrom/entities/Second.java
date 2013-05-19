package org.jcrom.entities;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class Second extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private String secondString;

    public Second() {
        super();
    }

    public String getSecondString() {
        return secondString;
    }

    public void setSecondString(String secondString) {
        this.secondString = secondString;
    }
}
