package org.jcrom;

import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class FinalEntity extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private final String immutableString;

    public FinalEntity(String immutableString) {
        this.immutableString = immutableString;
    }

    public String getImmutableString() {
        return immutableString;
    }
}
