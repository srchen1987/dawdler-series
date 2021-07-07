/*
 * Copyright 2002-2018 the original author or authors.
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class VfsResource extends AbstractResource {

	private final Object resource;

	public VfsResource(Object resource) {
		this.resource = resource;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return VfsUtils.getInputStream(this.resource);
	}

	@Override
	public boolean exists() {
		return VfsUtils.exists(this.resource);
	}

	@Override
	public boolean isReadable() {
		return VfsUtils.isReadable(this.resource);
	}

	@Override
	public URL getURL() throws IOException {
		try {
			return VfsUtils.getURL(this.resource);
		} catch (Exception ex) {
			throw new IOException("Failed to obtain URL for file " + this.resource, ex);
		}
	}

	@Override
	public URI getURI() throws IOException {
		try {
			return VfsUtils.getURI(this.resource);
		} catch (Exception ex) {
			throw new IOException("Failed to obtain URI for " + this.resource, ex);
		}
	}

	@Override
	public File getFile() throws IOException {
		return VfsUtils.getFile(this.resource);
	}

	@Override
	public long contentLength() throws IOException {
		return VfsUtils.getSize(this.resource);
	}

	@Override
	public long lastModified() throws IOException {
		return VfsUtils.getLastModified(this.resource);
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		if (!relativePath.startsWith(".") && relativePath.contains("/")) {
			try {
				return new VfsResource(VfsUtils.getChild(this.resource, relativePath));
			} catch (IOException ex) {
				// fall back to getRelative
			}
		}

		return new VfsResource(VfsUtils.getRelative(new URL(getURL(), relativePath)));
	}

	@Override
	public String getFilename() {
		return VfsUtils.getName(this.resource);
	}

	@Override
	public String getDescription() {
		return "VFS resource [" + this.resource + "]";
	}

	@Override
	public boolean equals(Object other) {
		return (this == other
				|| (other instanceof VfsResource && this.resource.equals(((VfsResource) other).resource)));
	}

	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}

}
