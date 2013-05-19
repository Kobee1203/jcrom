package org.jcrom;

import org.jcrom.annotations.JcrNode;

/**
 * Models a hierarchy node, base class for documents and folders.
 */
@JcrNode(nodeType = "nt:unstructured", classNameProperty = "className")
public abstract class HierarchyNode extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

}
