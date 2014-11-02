/*
 * This file is part of the Weedow jcrom (R) project.
 * Copyright (c) 2010-2014 Weedow Software Corp.
 * Authors: Nicolas Dos Santos
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY Weedow, 
 * Weedow DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://www.weedow.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving this program without disclosing
 * the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP, 
 * serving this program in a web application, shipping this program with a closed
 * source product.
 *
 * For more information, please contact Weedow Software Corp. at this
 * address: nicolas.dossantos@gmail.com
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
