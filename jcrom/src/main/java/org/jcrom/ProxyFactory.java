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
package org.jcrom;

import java.lang.reflect.Field;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.Session;
import net.sf.cglib.proxy.Enhancer;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.util.NodeFilter;

/**
 * Creates CGLIB proxies for lazy loading.
 *
 * @author Olafur Gauti Gudmundsson
 */
public class ProxyFactory {

    public static <T> T createChildNodeProxy(Class<T> c, Object parentObj, Session session, String containerPath,
            Mapper mapper, int depth, NodeFilter nodeFilter, boolean pathIsContainer) {
        ChildNodeLoader childNodeLoader = new ChildNodeLoader(c, parentObj, containerPath, session, mapper, depth, nodeFilter, pathIsContainer);
        return (T) Enhancer.create(c, childNodeLoader);
    }

    public static List createChildNodeListProxy(Class c, Object parentObj, Session session, String containerPath,
            Mapper mapper, int depth, NodeFilter nodeFilter, JcrChildNode jcrChildNode) {
        ChildNodeListLoader childNodeListLoader = new ChildNodeListLoader(c, parentObj, containerPath, session, mapper, depth, nodeFilter, jcrChildNode);
        return (List) Enhancer.create(List.class, childNodeListLoader);
    }

    public static <T> T createFileNodeProxy(Class<T> c, Node fileContainer, Object obj, JcrFileNode jcrFileNode, int depth, NodeFilter nodeFilter, Mapper mapper) {
        FileNodeLoader fileNodeLoader = new FileNodeLoader(c, fileContainer, obj, jcrFileNode, depth, nodeFilter, mapper);
        return (T) Enhancer.create(c, fileNodeLoader);
    }

    public static List createFileNodeListProxy(Class c, Node fileContainer, Object obj, JcrFileNode jcrFileNode, int depth, NodeFilter nodeFilter, Mapper mapper) {
        FileNodeListLoader fileNodeListLoader = new FileNodeListLoader(c, fileContainer, obj, jcrFileNode, depth, nodeFilter, mapper);
        return (List) Enhancer.create(List.class, fileNodeListLoader);
    }

    public static <T> T createReferenceProxy(Class<T> c, Object parentObject, String nodePath, String propertyName,
            Session session, Mapper mapper, int depth, NodeFilter nodeFilter, Field field) {
        ReferenceLoader refLoader = new ReferenceLoader(c, parentObject, nodePath, propertyName, session,
                mapper, depth, nodeFilter, field);
        return (T) Enhancer.create(c, refLoader);
    }

    public static List createReferenceListProxy(Class c, Object parentObject, String nodePath, String propertyName,
            Session session, Mapper mapper, int depth, NodeFilter nodeFilter, Field field) {
        ReferenceListLoader refListLoader = new ReferenceListLoader(c, parentObject, nodePath, propertyName, session,
                mapper, depth, nodeFilter, field);
        return (List) Enhancer.create(List.class, refListLoader);
    }
}
