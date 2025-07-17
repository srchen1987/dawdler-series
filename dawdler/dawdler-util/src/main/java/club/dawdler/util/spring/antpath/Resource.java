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

package club.dawdler.util.spring.antpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@SuppressWarnings("restriction")
public abstract class Resource extends sun.misc.Resource implements InputStreamSource {

	public abstract boolean exists();

	public boolean isReadable() {
		return exists();
	}
	public boolean isOpen() {
		return false;
	}

	public boolean isFile() {
		return false;
	}

	public abstract URI getURI() throws IOException;

	public abstract File getFile() throws IOException;

	public ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	public abstract long lastModified() throws IOException;

	public abstract Resource createRelative(String relativePath) throws IOException;

	public abstract String getFilename();

	public abstract String getDescription();

}
