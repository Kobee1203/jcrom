package org.jcrom

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import spock.lang.Shared;
import spock.lang.Specification;

abstract class AbstractJcrSpec extends Specification {
	
	@Shared Session session
	
	def setupSpec() {
		def configFile = AbstractJcrSpec.getResource('/config.xml').toURI()
		def homeDir = new File("target/repository/${getClass().simpleName}").absolutePath
		def config = RepositoryConfig.create(configFile, homeDir)
		
		def repository = new TransientRepository(config)
		
		session = repository.login(new SimpleCredentials('admin', ''.toCharArray()))
	}
	
	def cleanupSpec() {
		session.logout()
	}
	
	def cleanup() {
		session.refresh false
	}

}
