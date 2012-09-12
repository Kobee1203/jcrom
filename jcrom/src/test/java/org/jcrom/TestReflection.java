package org.jcrom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.jcr.Node;

import org.jcrom.util.NameFilter;
import org.jcrom.util.ReflectionUtils;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class TestReflection {

    @Test
    public void listClassesInPackage() throws Exception {

        Set<Class<?>> classes = ReflectionUtils.getClasses("org.jcrom.util");
        assertEquals(7, classes.size());
        assertTrue(classes.contains(NameFilter.class));

        Set<Class<?>> jcrClasses = ReflectionUtils.getClasses("javax.jcr");

        for (Class<?> c : jcrClasses) {
            System.out.println(c.getName());
        }

        assertTrue(jcrClasses.contains(Node.class));

        Set<Class<?>> classesToMap = ReflectionUtils.getClasses("org.jcrom.entities");

        assertEquals(2, classesToMap.size());
    }

}
