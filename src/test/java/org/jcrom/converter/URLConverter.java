/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
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

import java.net.MalformedURLException;
import java.net.URL;


/**
 * 
 * @author Nicolas Dos Santos
 */
public class URLConverter implements Converter<URL, String> {

    /**
     * Convert URL object to a string representation of this URL
     */
    @Override
    public String convertToJcrProperty(URL url) {
        return url.toString();
    }

    /**
     * Convert String representing an URL to an URL object
     */
    @Override
    public URL convertToEntityAttribute(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}