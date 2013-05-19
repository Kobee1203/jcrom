package org.jcrom;

import java.io.Serializable;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(mixinTypes = { "mix:referenceable" })
public class GrandParent extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    String uuid;
    @JcrChildNode
    Parent child;

    public GrandParent() {
    }

    public Parent getChild() {
        return child;
    }

    public void setChild(Parent child) {
        this.child = child;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
