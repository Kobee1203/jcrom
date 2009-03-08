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
package org.jcrom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * A simple implementation of the JcrDataProvider interface.
 * Developers can implement their own data provider if advanced or custom
 * functionality is needed.
 * 
 * <p>Thanks to Robin Wyles for adding content length.</p>
 * 
 * @author Olafur Gauti Gudmundsson
 */
public class JcrDataProviderImpl implements JcrDataProvider {

	protected final TYPE type;
	protected final byte[] bytes;
	protected final File file;
	protected final InputStream inputStream;
	protected final long contentLength;
	
	public JcrDataProviderImpl( TYPE type, byte[] bytes ) {
		this.type = type;
		this.bytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
		this.file = null;
		this.inputStream = null;
		this.contentLength = bytes.length;
	}
	
	public JcrDataProviderImpl( TYPE type, File file ) {
		this.type = type;
		this.file = file;
		this.bytes = null;
		this.inputStream = null;
		this.contentLength = file.length();
	}
	
	public JcrDataProviderImpl( TYPE type, InputStream inputStream ) {
		this.type = type;
		this.inputStream = inputStream;
		this.bytes = null;
		this.file = null;
		this.contentLength = -1;
	}
	
	public JcrDataProviderImpl(TYPE type, InputStream inputStream, long length) {
		this.type = type;
		this.inputStream = inputStream;
		this.bytes = null;
		this.file = null;
		this.contentLength = length;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public File getFile() {
		return file;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public TYPE getType() {
		return type;
	}

    public String getAsString( String charset ) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            inputStream.close();
        }
        return sb.toString();
    }

    public void writeToFile(File destination) throws IOException {
        if ( type == TYPE.BYTES ) {
            write(bytes, destination);
        } else if ( type == TYPE.STREAM ) {
            write(inputStream, destination);
        } else if ( type == TYPE.FILE ) {
            write(file, destination);
        }
    }
    
    protected static void write(InputStream in, File destination) throws IOException {
		if ( !destination.exists() ) {
			destination.createNewFile();
		}
        
        OutputStream out = new FileOutputStream(destination);
		try {
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
	        out.close();
		}
    }
    
	protected static void write( byte[] bytes, File destination ) throws IOException {

		if ( !destination.exists() ) {
			destination.createNewFile();
		}

		FileOutputStream fileOutputStream = new FileOutputStream(destination);
		try {
			fileOutputStream.write(bytes);
		} finally {
			fileOutputStream.close();
		}
	}

	protected static void write( File source, File destination ) throws IOException {

		FileInputStream in = new FileInputStream(source);
		FileOutputStream out = new FileOutputStream(destination);

		int doneCnt = -1, bufSize = 32768;
		byte buf[] = new byte[bufSize];

		try {
			while ((doneCnt = in.read(buf, 0, bufSize)) >= 0) {
				if ( doneCnt == 0 ) {
					Thread.yield();
                } else {
					out.write(buf, 0, doneCnt);
                }
			}
			out.flush();
		} finally {
			in.close();
			out.close();
		}
	}

	public long getContentLength() {
		return this.contentLength;
	}
}
