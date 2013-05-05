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

}