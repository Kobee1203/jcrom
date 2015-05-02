package org.jcrom;

import java.util.List;
import java.util.Map;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrReference;
import org.jcrom.type.DefaultTypeHandler;
import org.junit.Test;

public class TestValidator {

	private static class ChildNodeMap extends AbstractJcrEntity {

		private static final long serialVersionUID = 1L;

		@JcrChildNode
		private Map<String, A> map;

	}

	private static class ReferenceMap extends AbstractJcrEntity {

		private static final long serialVersionUID = 1L;

		@JcrReference
		private Map<String, A> map;

	}

	private static class ListMap extends AbstractJcrEntity {

		private static final long serialVersionUID = 1L;

		@JcrChildNode
		private Map<String, List<A>> map;

	}

	private static class InvalidChildNodeMap extends AbstractJcrEntity {

		private static final long serialVersionUID = 1L;

		@JcrChildNode
		private Map<String, Invalid> map;

	}

	private static class InvalidReferenceMap extends AbstractJcrEntity {

		private static final long serialVersionUID = 1L;

		@JcrReference
		private Map<String, Invalid> map;

	}

	private static class A extends AbstractJcrEntity {

		private static final long serialVersionUID = 1L;

		@JcrIdentifier
		private String id;

	}

	private static class Invalid {

		// no path and name

	}

	private static Validator newValidator() {
		Jcrom jcrom = new Jcrom();
		return new Validator(new DefaultTypeHandler(), jcrom);
	}

	@Test
	public void testValidatChildNodeMap() throws Exception {
		Validator validator = newValidator();
		validator.validate(ChildNodeMap.class, false);
	}

	@Test
	public void testValidateReferenceMap() throws Exception {
		Validator validator = newValidator();
		validator.validate(ReferenceMap.class, false);
	}

	@Test(expected = JcrMappingException.class)
	public void testInvalidReferenceMap() throws Exception {
		Validator validator = newValidator();
		validator.validate(InvalidReferenceMap.class, false);
	}

	@Test(expected = JcrMappingException.class)
	public void testInvalidChildNodeeMap() throws Exception {
		Validator validator = newValidator();
		validator.validate(InvalidChildNodeMap.class, false);
	}

	@Test
	public void testChildNodeMapWithListValue() throws Exception {
		Validator validator = newValidator();
		validator.validate(ListMap.class, false);
	}
}
