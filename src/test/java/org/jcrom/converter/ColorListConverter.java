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
package org.jcrom.converter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Nicolas Dos Santos
 */
public class ColorListConverter implements Converter<List<Color>, List<String>> {

    private final ColorConverter colorConverter = new ColorConverter();

    /**
     * Convert list of Color objects to a list of Strings with format red:green:blue:alpha
     */
    @Override
    public List<String> convertToJcrProperty(List<Color> colors) {
        List<String> list = new ArrayList<String>();
        for (Color color : colors) {
            list.add(colorConverter.convertToJcrProperty(color));
        }
        return list;
    }

    /**
     * Convert a list of Strings with format red:green:blue:alpha to a list of Color objects
     */
    @Override
    public List<Color> convertToEntityAttribute(List<String> colorStrings) {
        List<Color> list = new ArrayList<Color>();
        for (String colorString : colorStrings) {
            list.add(colorConverter.convertToEntityAttribute(colorString));
        }
        return list;
    }

}
