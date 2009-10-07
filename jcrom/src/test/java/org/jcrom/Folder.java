package org.jcrom;

import java.util.ArrayList;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 * Models a folder.
 */
@JcrNode(nodeType = "nt:unstructured", classNameProperty = "className", mixinTypes = { "mix:referenceable" })
public class Folder extends HierarchyNode {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    private String uuid;

    @JcrChildNode(containerNodeType = "nt:unstructured")
    private List<HierarchyNode> children = new ArrayList<HierarchyNode>();

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the uuid
     */
    public final String getUuid() {
        return uuid;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param uuid the uuid to set
     */
    public final void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * TODO Insert getter method comment here.
     * 
     * @return the children
     */
    public final List<HierarchyNode> getChildren() {
        return children;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param children the children to set
     */
    public final void setChildren(List<HierarchyNode> children) {
        this.children = children;
    }

}
