package org.jcrom;

import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrUUID;

/**
 * Models a Document, contains a file.
 */
@JcrNode(nodeType = "nt:unstructured", classNameProperty = "className", mixinTypes = { "mix:referenceable" })
public class Document extends HierarchyNode {

    private static final long serialVersionUID = 1L;

    @JcrUUID
    private String uuid;

    @JcrFileNode
    private JcrFile file;

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
     * @return the file
     */
    public final JcrFile getFile() {
        return file;
    }

    /**
     * TODO Insert setter method comment here.
     * 
     * @param file the file to set
     */
    public final void setFile(JcrFile file) {
        this.file = file;
    }

}
