package org.jcrom;

import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class Address extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    String street;
    @JcrProperty
    String postCode;

    public Address() {
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
