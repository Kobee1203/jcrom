/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
 * Authors: Olafur Gauti Gudmundsson, Nicolas Dos Santos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class EntityWithMapChildren extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private int[] multiInt;
    @JcrProperty
    private String[] multiString;

    @JcrProperty
    private Locale locale;
    @JcrProperty
    private Locale[] multiLocale;

    @JcrProperty
    private Map<String, String> strings;
    @JcrProperty
    private Map<String, String[]> stringArrays;
    @JcrProperty
    private Map<String, Integer> integers;
    @JcrProperty
    private Map<String, Integer[]> integerArrays;
    @JcrChildNode
    private Map<String, Object> objects;

    public EntityWithMapChildren() {
        strings = new HashMap<String, String>();
        stringArrays = new HashMap<String, String[]>();
        integers = new HashMap<String, Integer>();
        integerArrays = new HashMap<String, Integer[]>();
        objects = new LinkedHashMap<String, Object>();
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

    public void addIntegerArray(String name, Integer[] arr) {
        this.integerArrays.put(name, arr);
    }

    public Map<String, Integer> getIntegers() {
        return integers;
    }

    public void setIntegers(Map<String, Integer> integers) {
        this.integers = integers;
    }

    public void addInteger(String name, Integer value) {
        this.integers.put(name, value);
    }

    public Map<String, String[]> getStringArrays() {
        return stringArrays;
    }

    public void setStringArrays(Map<String, String[]> stringArrays) {
        this.stringArrays = stringArrays;
    }

    public void addStringArray(String name, String[] arr) {
        this.stringArrays.put(name, arr);
    }

    public Map<String, String> getStrings() {
        return strings;
    }

    public void setStrings(Map<String, String> strings) {
        this.strings = strings;
    }

    public void addString(String name, String value) {
        this.strings.put(name, value);
    }

    public Map<String, Object> getObjects() {
        return objects;
    }

    public void setObjects(Map<String, Object> objects) {
        this.objects = objects;
    }

    public void addObject(String name, Object value) {
        this.objects.put(name, value);
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
