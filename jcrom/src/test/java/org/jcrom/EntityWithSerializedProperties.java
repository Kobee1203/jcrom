package org.jcrom;

import org.jcrom.annotations.JcrSerializedProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class EntityWithSerializedProperties extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrSerializedProperty
    private Parent parent;

    public EntityWithSerializedProperties() {
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

}
