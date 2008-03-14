package org.jcrom;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class EntityWithMapChildren extends AbstractJcrEntity {

	@JcrProperty private int[] multiInt;
	@JcrProperty private String[] multiString;
	
	@JcrProperty private Locale locale;
	@JcrProperty private Locale[] multiLocale;
	
	@JcrChildNode private Map<String,String> strings;
	@JcrChildNode private Map<String,String[]> stringArrays;
	@JcrChildNode private Map<String,Integer> integers;
	@JcrChildNode private Map<String,Integer[]> integerArrays;
	
	public EntityWithMapChildren() {
		strings = new HashMap<String,String>();
		stringArrays = new HashMap<String,String[]>();
		integers = new HashMap<String,Integer>();
		integerArrays = new HashMap<String,Integer[]>();
	}

	public int[] getMultiInt() {
		return multiInt;
	}

	public void setMultiInt(int[] multiInt) {
		this.multiInt = multiInt;
	}

	public String[] getMultiString() {
		return multiString;
	}

	public void setMultiString(String[] multiString) {
		this.multiString = multiString;
	}

	public Map<String, Integer[]> getIntegerArrays() {
		return integerArrays;
	}

	public void setIntegerArrays(Map<String, Integer[]> integerArrays) {
		this.integerArrays = integerArrays;
	}
	public void addIntegerArray( String name, Integer[] arr ) {
		this.integerArrays.put(name, arr);
	}

	public Map<String, Integer> getIntegers() {
		return integers;
	}

	public void setIntegers(Map<String, Integer> integers) {
		this.integers = integers;
	}
	public void addInteger( String name, Integer value ) {
		this.integers.put(name, value);
	}

	public Map<String, String[]> getStringArrays() {
		return stringArrays;
	}

	public void setStringArrays(Map<String, String[]> stringArrays) {
		this.stringArrays = stringArrays;
	}
	public void addStringArray( String name, String[] arr ) {
		this.stringArrays.put(name, arr);
	}

	public Map<String, String> getStrings() {
		return strings;
	}

	public void setStrings(Map<String, String> strings) {
		this.strings = strings;
	}
	public void addString( String name, String value ) {
		this.strings.put(name, value);
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale[] getMultiLocale() {
		return multiLocale;
	}

	public void setMultiLocale(Locale[] multiLocale) {
		this.multiLocale = multiLocale;
	}
	
}
