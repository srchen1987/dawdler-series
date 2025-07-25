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
package club.dawdler.serverplug.load.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * 可序列化的远程模版类传输对象
 */
public class RemoteFiles implements Serializable {
	private static final long serialVersionUID = 5204063780120035205L;
	private List<RemoteFile> files;

	public List<RemoteFile> getFiles() {
		return files;
	}

	public void setFiles(List<RemoteFile> files) {
		this.files = files;
	}

	public class RemoteFile implements Serializable {
		private static final long serialVersionUID = 1090097237519066361L;
		private String filename;
		private byte[] data;

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

	}
}
