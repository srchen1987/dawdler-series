/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anywide.dawdler.util.spring.antpath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public abstract class AbstractResource extends Resource {

	@Override
	public boolean exists() {
		if (isFile()) {
			try {
				return getFile().exists();
			} catch (IOException ex) {
			}
		}
		try {
			getInputStream().close();
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}

	@Override
	public boolean isReadable() {
		return exists();
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public URL getURL() {
		throw null;
	}

	@Override
	public URI getURI() throws IOException {
		URL url = getURL();
		try {
			return ResourceUtils.toURI(url);
		} catch (URISyntaxException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public File getFile() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
	}

	@Override
	public ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	@Override
	public int getContentLength() throws IOException {
		InputStream is = getInputStream();

		try {
			return is.available();
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
			}
		}
	}

	@Override
	public long lastModified() throws IOException, URISyntaxException {
		File fileToCheck = getFileForLastModifiedCheck();
		long lastModified = fileToCheck.lastModified();
		if (lastModified == 0L && !fileToCheck.exists()) {
			throw new FileNotFoundException(getDescription()
					+ " cannot be resolved in the file system for checking its last-modified timestamp");
		}
		return lastModified;
	}

	protected File getFileForLastModifiedCheck() throws IOException, URISyntaxException {
		return getFile();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException, URISyntaxException {
		throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
	}

	@Override
	public String getFilename() {
		return null;
	}

	@Override
	public boolean equals(Object other) {
		return (this == other
				|| (other instanceof Resource && ((Resource) other).getDescription().equals(getDescription())));
	}

	@Override
	public int hashCode() {
		return getDescription().hashCode();
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
