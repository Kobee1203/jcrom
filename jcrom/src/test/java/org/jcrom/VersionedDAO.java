package org.jcrom;

import javax.jcr.Session;
import org.jcrom.dao.AbstractJcrDAO;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class VersionedDAO extends AbstractJcrDAO<VersionedEntity> {

	private static final String[] MIXIN_TYPES = {"mix:versionable"};
	
	public VersionedDAO( Session session, Jcrom jcrom ) {
		super(VersionedEntity.class, session, jcrom, MIXIN_TYPES);
	}
}
