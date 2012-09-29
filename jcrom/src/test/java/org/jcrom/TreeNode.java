package org.jcrom;

import java.util.ArrayList;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(mixinTypes = { "mix:referenceable" }, classNameProperty = "className")
public class TreeNode extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    private String uuid;
    @JcrChildNode(lazy = true)
    private List<TreeNode> children;

    public TreeNode() {
        this(null);
    }

    public TreeNode(String name) {
        super();
        this.name = name;
        this.children = new ArrayList<TreeNode>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }
}
