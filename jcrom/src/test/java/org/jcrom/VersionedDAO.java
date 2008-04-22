package org.jcrom;

import javax.jcr.Session;
import org.jcrom.dao.AbstractJcrDAO;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class VersionedDAO extends AbstractJcrDAO<VersionedEntity> {
	
	public VersionedDAO( Session session, Jcrom jcrom ) {
		super(VersionedEntity.class, session, jcrom);
	}
}
