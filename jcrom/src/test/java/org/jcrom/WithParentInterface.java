package org.jcrom;

import org.jcrom.annotations.JcrParentNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class WithParentInterface extends AbstractJcrEntity {

    @JcrParentNode private Shape parent;
    
    public WithParentInterface() {
        super();
    }

    public Shape getParent() {
        return parent;
    }

    public void setParent(Shape parent) {
        this.parent = parent;
    }
    
}
