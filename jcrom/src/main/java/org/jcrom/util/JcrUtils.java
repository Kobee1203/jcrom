/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom.util;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.jcrom.JcrMappingException;

/**
 * Contains utilities used for JCR nodes
 * 
 * @author Nicolas Dos Santos
 */
public final class JcrUtils {

    private JcrUtils() {
    }

    public static boolean hasMixinType(Node node, String mixinType) throws RepositoryException {
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            if (nodeType.getName().equals(mixinType)) {
                return true;
            }
        }
        return false;
    }

    public static LockManager getLockManager(Session session) throws RepositoryException {
        LockManager lockMgr = session.getWorkspace().getLockManager();
        return lockMgr;
    }

    public static VersionManager getVersionManager(Session session) throws RepositoryException {
        VersionManager versionMgr = session.getWorkspace().getVersionManager();
        return versionMgr;
    }

    public static void checkout(Node node) throws RepositoryException {
        getVersionManager(node.getSession()).checkout(node.getPath());
    }

    public static Version checkin(Node node) throws RepositoryException {
        return getVersionManager(node.getSession()).checkin(node.getPath());
    }

    public static void checkinRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkinRecursively(it.nextNode());
            }
            if (node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                //node.checkin();
                checkin(node);
            }

        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform check-in", e);
        }
    }

    public static void checkoutRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkoutRecursively(it.nextNode());
            }
            if (!node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                //node.checkout();
                checkout(node);
            }

        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform check-out", e);
        }
    }

    /**
     * @see javax.jcr.lock.LockManager#lock(String, boolean, boolean, long, String)
     */
    public static void lock(Node node, boolean isDeep, boolean isSessionScoped, long timeoutHint, String ownerInfo) {
        try {
            getLockManager(node.getSession()).lock(node.getPath(), isDeep, isSessionScoped, timeoutHint, ownerInfo);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform lock", e);
        }
    }

    /**
     * @see javax.jcr.lock.LockManager#unlock(String)
     */
    public static void unlock(Node node) {
        try {
            getLockManager(node.getSession()).unlock(node.getPath());
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform unlock", e);
        }
    }
}
