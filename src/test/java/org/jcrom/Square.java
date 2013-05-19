package org.jcrom;

import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class Square extends Rectangle {

    private static final long serialVersionUID = 1L;

    @JcrChildNode
    private WithParentInterface child;

    public Square() {
    }

    public Square(double height, double width) {
        super(height, width);
        if (height != width) {
            throw new IllegalArgumentException("This is not a square!");
        }
    }

    public WithParentInterface getChild() {
        return child;
    }

    public void setChild(WithParentInterface child) {
        this.child = child;
    }

}
