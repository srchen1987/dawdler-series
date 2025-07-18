/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.clientplug.velocity.resource.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import club.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * velocity资源加载器，从classpath中加载模板
 */
public class DawdlerVelocityResourceLoader extends ResourceLoader {
	public DawdlerVelocityResourceLoader() {
	}

	public void init(ExtProperties configuration) {
		this.log.trace("ClasspathResourceLoader: initialization complete.");
	}

	public Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException {
		Reader result = null;
		if (name == null || name.length() == 0) {
			throw new ResourceNotFoundException("No template name provided");
		} else {
			InputStream rawStream = null;

			try {
				rawStream = DawdlerTool.getResourceFromClassPath(name);
				if (rawStream != null) {
					result = this.buildReader(rawStream, encoding);
				}
			} catch (Exception var8) {
				if (rawStream != null) {
					try {
						rawStream.close();
					} catch (IOException var7) {
					}
				}

				throw new ResourceNotFoundException("ClasspathResourceLoader problem with template: " + name, var8,
						this.rsvc.getLogContext().getStackTrace());
			}

			if (result == null) {
				String msg = "ClasspathResourceLoader Error: cannot find resource " + name;
				throw new ResourceNotFoundException(msg, (Throwable) null, this.rsvc.getLogContext().getStackTrace());
			} else {
				return result;
			}
		}
	}

	public boolean isSourceModified(Resource resource) {
		return false;
	}

	public long getLastModified(Resource resource) {
		return 0L;
	}

}
