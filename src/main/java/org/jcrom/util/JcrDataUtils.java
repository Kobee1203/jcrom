/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.tika.Tika;
import org.jcrom.JcrDataProvider;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.JcrMappingException;
import org.jcrom.util.io.FileUtils;
import org.jcrom.util.io.IOUtils;

/**
 * Contains utilities used for JCR file node
 *
 * @author Nicolas Dos Santos
 */
public final class JcrDataUtils {

    private JcrDataUtils() {
    }

    public static JcrFile fromFile(String name, File file) {
        JcrFile jcrFile = fromFile(name, file, null);
        try {
            jcrFile.setMimeType(new Tika().detect(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jcrFile;
    }

    public static JcrFile fromFile(String name, File file, String mimeType) {
        return fromFile(name, file, mimeType, null);
    }

    public static JcrFile fromFile(String name, File file, String mimeType, String encoding) {
        JcrFile jcrFile = new JcrFile();
        jcrFile.setName(name);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(file.lastModified());

        jcrFile.setLastModified(cal);
        jcrFile.setMimeType(mimeType);
        jcrFile.setEncoding(encoding);
        jcrFile.setDataProvider(new JcrDataProviderImpl(file));

        return jcrFile;
    }

    public static JcrFile fromInputStream(String name, InputStream input) {
        JcrFile jcrFile = fromInputStream(name, input, null);
        try {
            jcrFile.setMimeType(new Tika().detect(input));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jcrFile;
    }

    public static JcrFile fromInputStream(String name, InputStream input, String mimeType) {
        return fromInputStream(name, input, mimeType, null);
    }

    public static JcrFile fromInputStream(String name, InputStream input, String mimeType, String encoding) {
        JcrFile jcrFile = new JcrFile();
        jcrFile.setName(name);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis());

        jcrFile.setLastModified(cal);
        jcrFile.setMimeType(mimeType);
        jcrFile.setEncoding(encoding);
        jcrFile.setDataProvider(new JcrDataProviderImpl(input));

        return jcrFile;
    }

    public static JcrFile fromByteArray(String name, byte[] bytes) {
        JcrFile jcrFile = fromByteArray(name, bytes, null);
        jcrFile.setMimeType(new Tika().detect(bytes));

        return jcrFile;
    }

    public static JcrFile fromByteArray(String name, byte[] bytes, String mimeType) {
        return fromByteArray(name, bytes, mimeType, null);
    }

    public static JcrFile fromByteArray(String name, byte[] bytes, String mimeType, String encoding) {
        JcrFile jcrFile = new JcrFile();
        jcrFile.setName(name);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis());

        jcrFile.setLastModified(cal);
        jcrFile.setMimeType(mimeType);
        jcrFile.setEncoding(encoding);
        jcrFile.setDataProvider(new JcrDataProviderImpl(bytes));

        return jcrFile;
    }

    public static String toString(JcrDataProvider dataProvider, String encoding) {
        String data = null;
        switch (dataProvider.getType()) {
            case BYTES:
                byte[] b = dataProvider.getBytes();
                try {
                    data = new String(b, encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new JcrMappingException(e.getMessage(), e);
                }
                break;
            case FILE:
                File f = dataProvider.getFile();
                try {
                    data = FileUtils.readFileToString(f, encoding);
                } catch (IOException e) {
                    throw new JcrMappingException(e.getMessage(), e);
                }
                break;
            case STREAM:
                InputStream input = dataProvider.getInputStream();
                try {
                    data = IOUtils.toString(input, encoding);
                } catch (IOException e) {
                    throw new JcrMappingException(e.getMessage(), e);
                }
                break;
            default:
                throw new IllegalArgumentException("it should never pass this way !!. The type of content found is '" + dataProvider.getType() + "'");
        }

        return data;
    }

    public static byte[] toByteArray(JcrDataProvider dataProvider) {
        byte[] data = null;
        switch (dataProvider.getType()) {
            case BYTES:
                data = dataProvider.getBytes();
                break;
            case FILE:
                File f = dataProvider.getFile();
                try {
                    data = FileUtils.readFileToByteArray(f);
                } catch (IOException e) {
                    throw new JcrMappingException(e.getMessage(), e);
                }
                break;
            case STREAM:
                InputStream input = dataProvider.getInputStream();
                try {
                    data = IOUtils.toByteArray(input);
                } catch (IOException e) {
                    throw new JcrMappingException(e.getMessage(), e);
                }
                break;
            default:
                throw new IllegalArgumentException("it should never pass this way !!. The type of content found is '" + dataProvider.getType() + "'");
        }

        return data;
    }

    public static InputStream toStream(JcrDataProvider dataProvider) {
        InputStream data = null;
        switch (dataProvider.getType()) {
            case BYTES:
                byte[] b = dataProvider.getBytes();
                data = new ByteArrayInputStream(b);
                break;
            case FILE:
                File f = dataProvider.getFile();
                try {
                    data = FileUtils.openInputStream(f);
                } catch (IOException e) {
                    throw new JcrMappingException(e.getMessage(), e);
                }
                break;
            case STREAM:
                data = dataProvider.getInputStream();
                break;
            default:
                throw new IllegalArgumentException("it should never pass this way !!. The type of content found is '" + dataProvider.getType() + "'");
        }

        return data;
    }
}
