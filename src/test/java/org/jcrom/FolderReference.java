package org.jcrom;

import java.util.List;

import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrReference;

/**
 * Models a reference to a Folder.
 */
@JcrNode(nodeType = "nt:unstructured", classNameProperty = "className")
public class FolderReference extends HierarchyNode {

    private static final long serialVersionUID = 1L;

    @JcrReference
    private Folder reference;

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the reference
     */
    public final Folder getReference() {
        return reference;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param reference the reference to set
     */
    public final void setReference(Folder reference) {
        this.reference = reference;
    }

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the children
     */
    public final List<HierarchyNode> getChildren() {
        return reference.getChildren();
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param children the children to set
     */
    public final void setChildren(List<HierarchyNode> children) {
        reference.setChildren(children);
    }
}
