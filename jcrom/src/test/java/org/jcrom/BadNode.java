package org.jcrom;

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrUUID;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(mixinTypes = {"mix:referenceable"})
public class BadNode {

    @JcrName public String name="badNode";
    @JcrPath public String path;
    @JcrUUID public String uuid;
    @JcrReference public BadNode reference;
    @JcrProperty public String body;
}
