package org.jcrom;

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

@JcrNode(nodeType = "cq:PageContent")
public class PageContentNode {

    @JcrName
    private String name;
    @JcrPath
    private String path;
    @JcrProperty(name = "sling:resourceType")
    private String resourceType;
    @JcrProperty(name = "jcr:title")
    private String title;
    @JcrProperty(name = "jcr:description")
    private String description;

    // BELOW PROPERTY NOT ABLE TO INSERT INTO JCR REPO
    @JcrProperty(name = "cq:tags")
    private String[] tags;

    //@JcrProperty(name = "cq:tags")
    //List<String> tags;

    public PageContentNode() {
        // tags = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    /*
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }
    */
}