package org.jcrom;

import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class UserProfile extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrChildNode(createContainerNode = false)
    Address address;

    public UserProfile() {
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
