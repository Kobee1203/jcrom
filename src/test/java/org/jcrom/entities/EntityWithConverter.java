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
package org.jcrom.entities;

import java.awt.Color;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.converter.ColorConverter;
import org.jcrom.converter.ColorListConverter;
import org.jcrom.converter.URLConverter;
import org.jcrom.converter.URLMapConverter;

/**
 * 
 * @author Nicolas Dos Santos
 */
public class EntityWithConverter extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty(converter = ColorConverter.class)
    private Color color;

    @JcrProperty(converter = ColorListConverter.class)
    private List<Color> colors;

    @JcrProperty(converter = URLConverter.class)
    private URL url;

    @JcrProperty(converter = URLMapConverter.class)
    private Map<String, URL> urlMap;

    public EntityWithConverter() {
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, URL> getUrlMap() {
        return urlMap;
    }

    public void setUrlMap(Map<String, URL> urlMap) {
        this.urlMap = urlMap;
    }

}
