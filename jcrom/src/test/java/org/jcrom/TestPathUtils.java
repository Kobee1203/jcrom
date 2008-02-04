package org.jcrom;

import org.jcrom.util.PathUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class TestPathUtils {

	@Test
	public void testPathUtils() {
		
		String path1 = " Hello, world!";
		assertEquals("Hello_world", PathUtils.createValidName(path1));
		
		String path2 = "how_are_you?";
		assertEquals("how_are_you", PathUtils.createValidName(path2));
	}
}
