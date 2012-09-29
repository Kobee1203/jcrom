package org.jcrom;

import java.util.ArrayList;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(mixinTypes = { "mix:referenceable" })
public class Tree extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    String uuid;
    @JcrChildNode(lazy = true)
    private TreeNode templateNode;
    @JcrChildNode(lazy = true)
    private List<TreeNode> children;

    @JcrReference(lazy = true, byPath = true)
    private TreeNode startNode;
    @JcrReference(lazy = true)
    private List<TreeNode> favourites;

    @JcrChildNode(lazy = true)
    private LazyInterface lazyObject;
    @JcrChildNode(lazy = true)
    private List<LazyInterface> lazyObjects;

    public Tree() {
        super();
        this.children = new ArrayList<TreeNode>();
        this.favourites = new ArrayList<TreeNode>();
        this.lazyObjects = new ArrayList<LazyInterface>();
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

    public List<TreeNode> getFavourites() {
        return favourites;
    }

    public void setFavourites(List<TreeNode> favourites) {
        this.favourites = favourites;
    }

    public void addFavourite(TreeNode favourite) {
        favourites.add(favourite);
    }

    public TreeNode getStartNode() {
        return startNode;
    }

    public void setStartNode(TreeNode startNode) {
        this.startNode = startNode;
    }

    public TreeNode getTemplateNode() {
        return templateNode;
    }

    public void setTemplateNode(TreeNode templateNode) {
        this.templateNode = templateNode;
    }

    public LazyInterface getLazyObject() {
        return lazyObject;
    }

    public void setLazyObject(LazyInterface lazyObject) {
        this.lazyObject = lazyObject;
    }

    public List<LazyInterface> getLazyObjects() {
        return lazyObjects;
    }

    public void setLazyObjects(List<LazyInterface> lazyObjects) {
        this.lazyObjects = lazyObjects;
    }

    public void addLazyObject(LazyInterface lazy) {
        lazyObjects.add(lazy);
    }
}
