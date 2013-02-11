package org.jcrom

import java.lang.reflect.Array

import javax.jcr.PropertyType;
import javax.jcr.ReferentialIntegrityException;

import org.jcrom.annotations.JcrIdentifier
import org.jcrom.annotations.JcrName
import org.jcrom.annotations.JcrPath
import org.jcrom.annotations.JcrReference

class JcrReferenceSpec extends AbstractJcrSpec {

	def 'assert creation of weak reference'() {
		setup: 'initialise jcrom'
		Jcrom jcrom = []
		jcrom.map(A1)
		jcrom.map(A2)
		jcrom.map(A3)
		jcrom.map(B)
		
		and: 'intialise mappable objects'
		B instanceB = [id: '12345', name: 'instanceB']
		A1 instanceA1 = [name: 'instanceA1', bRef: instanceB]
		A2 instanceA2 = [name: 'instanceA2', bRef: instanceB]
		A3 instanceA3 = [name: 'instanceA3', bRef: instanceB]
		
		when:
		jcrom.addNode(session.rootNode, instanceB, ['mix:referenceable'] as String[])
		jcrom.addNode(session.rootNode, instanceA1)
		jcrom.addNode(session.rootNode, instanceA2)
		jcrom.addNode(session.rootNode, instanceA3)
		def instanceBID = session.rootNode.getNode('instanceB').identifier
		session.rootNode.getNode('instanceB').remove()
		
		then: 'A1 holds a weak reference'
		println session.rootNode.getNode('instanceA1').getProperty('bRef').string
		session.rootNode.getNode('instanceA1').getProperty('bRef').type == PropertyType.WEAKREFERENCE
		session.rootNode.getNode('instanceA1').getProperty('bRef').string == instanceBID
		
		and: 'A2 holds a reference'
		println session.rootNode.getNode('instanceA2').getProperty('bRef').string
		session.rootNode.getNode('instanceA2').getProperty('bRef').type == PropertyType.REFERENCE
		session.rootNode.getNode('instanceA2').getProperty('bRef').string == instanceBID
		
		and: 'A3 holds a reference'
		println session.rootNode.getNode('instanceA3').getProperty('bRef').string
		session.rootNode.getNode('instanceA3').getProperty('bRef').type == PropertyType.REFERENCE
		session.rootNode.getNode('instanceA3').getProperty('bRef').string == instanceBID
	}
	
	def 'assert referential integrity using weak reference'() {
		setup: 'initialise jcrom'
		Jcrom jcrom = []
		jcrom.map(A1)
		jcrom.map(B)
		
		and: 'intialise mappable objects'
		B instanceB = [id: '12345', name: 'instanceB']
		A1 instanceA1 = [name: 'instanceA1', bRef: instanceB]
		
		when:
		jcrom.addNode(session.rootNode, instanceB, ['mix:referenceable'] as String[])
		def instanceBID = session.rootNode.getNode('instanceB').identifier
		jcrom.addNode(session.rootNode, instanceA1)
		session.rootNode.getNode('instanceB').remove()
		session.save()

		then:
		session.rootNode.getNode('instanceA1').getProperty('bRef').type == PropertyType.WEAKREFERENCE
		session.rootNode.getNode('instanceA1').getProperty('bRef').string == instanceBID
	}
	
	def 'assert no referential integrity using default reference'() {
		setup: 'initialise jcrom'
		Jcrom jcrom = []
		jcrom.map(A3)
		jcrom.map(B)
		
		and: 'intialise mappable objects'
		B instanceB = [id: '12345', name: 'instanceB']
		A3 instanceA3 = [name: 'instanceA3', bRef: instanceB]
		
		when:
		jcrom.addNode(session.rootNode, instanceB, ['mix:referenceable'] as String[])
		jcrom.addNode(session.rootNode, instanceA3)
		session.rootNode.getNode('instanceB').remove()
		session.save()

		then:
		thrown(ReferentialIntegrityException)
	}

	private class A1 {
		@JcrName
		String name
		
		@JcrPath
		String path
		
		@JcrReference(weak=true)
		B bRef
	}
	
	private class A2 {
		@JcrName
		String name
		
		@JcrPath
		String path
		
		@JcrReference(weak=false)
		B bRef
	}
	
	private class A3 {
		@JcrName
		String name
		
		@JcrPath
		String path
		
		@JcrReference
		B bRef
	}

	private class B {
		@JcrIdentifier
		String id
		
		@JcrName
		String name
		
		@JcrPath
		String path
	}
}
