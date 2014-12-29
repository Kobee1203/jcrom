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
package org.jcrom.converter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Nicolas Dos Santos
 */
public class URLMapConverter implements Converter<Map<String, URL>, Map<String, String>> {

    private final URLConverter urlConverter = new URLConverter();

    /**
     * Convert map of URL to map of strings representation of this URL
     */
    @Override
    public Map<String, String> convertToJcrProperty(Map<String, URL> urlMap) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, URL> entry : urlMap.entrySet()) {
            map.put(entry.getKey(), urlConverter.convertToJcrProperty(entry.getValue()));
        }
        return map;
    }

    /**
     * Convert map of String representing an URL to a map of URL
     */
    @Override
    public Map<String, URL> convertToEntityAttribute(Map<String, String> urlStringMap) {
        Map<String, URL> map = new HashMap<String, URL>();
        for (Map.Entry<String, String> entry : urlStringMap.entrySet()) {
            map.put(entry.getKey(), urlConverter.convertToEntityAttribute(entry.getValue()));
        }
        return map;
    }

}
