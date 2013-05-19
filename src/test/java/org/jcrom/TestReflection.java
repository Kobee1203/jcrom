package org.jcrom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.jcr.Node;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.util.ReflectionUtils;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class TestReflection {

    @Test
    public void listClassesInPackage() throws Exception {

        Set<Class<?>> classes = ReflectionUtils.getClasses("org.jcrom.annotations");
        assertEquals(19, classes.size());
        assertTrue(classes.contains(JcrChildNode.class));
        assertTrue(classes.contains(JcrFileNode.LoadType.class));

        Set<Class<?>> jcrClasses = ReflectionUtils.getClasses("javax.jcr");

        for (Class<?> c : jcrClasses) {
            System.out.println(c.getName());
        }

        assertTrue(jcrClasses.contains(Node.class));

        Set<Class<?>> classesToMap = ReflectionUtils.getClasses("org.jcrom.entities");

        assertEquals(2, classesToMap.size());
    }

}
