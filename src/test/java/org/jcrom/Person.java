package org.jcrom;

import java.util.List;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

/**
 * @author Decebal Suiu
 */
@JcrNode
public class Person {

	@JcrPath
	private String path;

	@JcrName
	private String name;

	@JcrProperty
	private int age;

	@JcrProperty
	private List<String> phones;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("name = ");
		sb.append(name);
		sb.append(",age = ");
		sb.append(age);
		sb.append(",phones = ");
		sb.append(phones);

		return sb.toString();
	}

}
