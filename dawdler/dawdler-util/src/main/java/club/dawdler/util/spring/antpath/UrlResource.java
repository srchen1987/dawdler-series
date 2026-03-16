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

package club.dawdler.util.spring.antpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class UrlResource extends AbstractFileResolvingResource {

	private final URI uri;

	private final URL url;

	private volatile URL cleanedUrl;

	public UrlResource(URI uri) throws MalformedURLException {
		this.uri = uri;
		this.url = uri.toURL();
	}

	public UrlResource(URL url) {
		this.uri = null;
		this.url = url;
	}

	public UrlResource(String path) throws MalformedURLException {
		URI uri;
		URL url;
		try {
			uri = ResourceUtils.toURI(path);
			url = uri.toURL();
		} catch (URISyntaxException | IllegalArgumentException ex) {
			uri = null;
			url = ResourceUtils.toURL(path);
		}

		this.uri = uri;
		this.url = url;
		this.cleanedUrl = getCleanedUrl(this.url, path);
		;
	}

	public UrlResource(String protocol, String location) throws MalformedURLException {
		this(protocol, location, null);
	}

	public UrlResource(String protocol, String location, String fragment) throws MalformedURLException {
		try {
			this.uri = new URI(protocol, location, fragment);
			this.url = this.uri.toURL();
		} catch (URISyntaxException ex) {
			MalformedURLException exToThrow = new MalformedURLException(ex.getMessage());
			exToThrow.initCause(ex);
			throw exToThrow;
		}
	}

	private static URL getCleanedUrl(URL originalUrl, String originalPath) {
		String cleanedPath = StringUtils.cleanPath(originalPath);
		if (!cleanedPath.equals(originalPath)) {
			try {
				return new URI(cleanedPath).toURL();
			} catch (MalformedURLException | URISyntaxException ex) {
			}
		}
		return originalUrl;
	}

	private URL getCleanedUrl() {
		URL cleanedUrl = this.cleanedUrl;
		if (cleanedUrl != null) {
			return cleanedUrl;
		}
		cleanedUrl = getCleanedUrl(this.url, (this.uri != null ? this.uri : this.url).toString());
		this.cleanedUrl = cleanedUrl;
		return cleanedUrl;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		URLConnection con = this.url.openConnection();
		ResourceUtils.useCachesIfNecessary(con);
		try {
			return con.getInputStream();
		} catch (IOException ex) {
			// Close the HTTP connection (if applicable).
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).disconnect();
			}
			throw ex;
		}
	}

	@Override
	public URL getURL() {
		return this.url;
	}

	@Override
	public URI getURI() throws IOException {
		if (this.uri != null) {
			return this.uri;
		} else {
			return super.getURI();
		}
	}

	@Override
	public boolean isFile() {
		if (this.uri != null) {
			return super.isFile(this.uri);
		} else {
			return super.isFile();
		}
	}

	@Override
	public File getFile() throws IOException {
		if (this.uri != null) {
			return super.getFile(this.uri);
		} else {
			return super.getFile();
		}
	}

	@Override
	public Resource createRelative(String relativePath) throws MalformedURLException {
		return new UrlResource(createRelativeURL(relativePath));
	}

	protected URL createRelativeURL(String relativePath) throws MalformedURLException {
		relativePath = StringUtils.replace(relativePath, "#", "%23");
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}
		return ResourceUtils.toRelativeURL(this.url, relativePath);
	}

	@Override
	public String getFilename() {
		return StringUtils.getFilename(getCleanedUrl().getPath());
	}

	@Override
	public String getDescription() {
		return "URL [" + this.url + "]";
	}

	@Override
	public boolean equals(Object other) {
		return (this == other
				|| (other instanceof UrlResource && getCleanedUrl().equals(((UrlResource) other).getCleanedUrl())));
	}

	@Override
	public int hashCode() {
		return getCleanedUrl().hashCode();
	}

}
