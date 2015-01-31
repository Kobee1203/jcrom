package org.jcrom.modeshape;

import static org.junit.Assert.assertTrue;

import org.jcrom.Jcrom;
import org.jcrom.entities.Child;
import org.jcrom.entities.Parent;
import org.junit.Test;
import org.modeshape.test.ModeShapeSingleUseTest;

/**
 * Thanks to Antoine Mischler for identifying this problem and contributing this test case.
 *
 * @author Nicolas Dos Santos
 */
public class TestUpdateChildNodePerformance extends ModeShapeSingleUseTest {

    @Test
    public void testPerformance() {
        Jcrom jcrom = new Jcrom(false, true);
        jcrom.map(Parent.class);
        jcrom.map(Child.class);
        ParentDAO parentDAO = new ParentDAO(session, jcrom);
        Parent parent = new Parent();
        parent.setName("Parent");
        parent.setPath("/");
        parentDAO.create(parent);
        for (int i = 0; i < 1000; i++) {
            Child child = new Child();
            child.setName("Child " + i);
            parent.getChildren().add(child);
        }
        long startTime = System.currentTimeMillis();
        parentDAO.update(parent);
        long firstUpdate = System.currentTimeMillis();
        long firstUpdateTime = firstUpdate - startTime;
        System.out.println("First update took: " + firstUpdateTime + " ms");
        assertTrue(firstUpdateTime < 10000);
        parentDAO.update(parent);
        long secondUpdateTime = System.currentTimeMillis() - firstUpdate;
        System.out.println("Second update took: " + secondUpdateTime + " ms");
        assertTrue(secondUpdateTime < 8000);
    }

}