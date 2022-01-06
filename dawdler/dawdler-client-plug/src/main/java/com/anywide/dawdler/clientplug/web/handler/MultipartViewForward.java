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
package com.anywide.dawdler.clientplug.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.upload.UploadFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MultipartViewForward.java
 * @Description 支持获取附件上传的forward
 * @date 2007年4月19日
 * @email suxuan696@gmail.com
 */
public class MultipartViewForward extends ViewForward {
	private static final Logger logger = LoggerFactory.getLogger(MultipartViewForward.class);
	private static final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();

	static {
		diskFileItemFactory.setSizeThreshold(1024 * 1024);
	}

	private final Map<String, List<String>> params = new HashMap<>();
	private final Map<String, List<UploadFile>> fileParams = new HashMap<>();

	public MultipartViewForward(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	public void parse(long uploadSizeMax, long uploadPerSizeMax)
			throws FileUploadException, UnsupportedEncodingException {
		ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
		if (uploadSizeMax > 0)
			upload.setSizeMax(uploadSizeMax);
		if (uploadPerSizeMax > 0)
			upload.setFileSizeMax(uploadPerSizeMax);
		List<FileItem> fileItems;
		fileItems = upload.parseRequest(request);
		for (FileItem fileItem : fileItems) {
			String fieldName = fileItem.getFieldName();
			if (fileItem.isFormField()) {
				List<String> list = params.get(fieldName);
				if (list == null) {
					list = new ArrayList<>();
					params.put(fieldName, list);
				}
				list.add(fileItem.getString("utf-8"));
			} else {
				List<UploadFile> list = fileParams.get(fieldName);
				if (list == null) {
					list = new ArrayList<>();
					fileParams.put(fieldName, list);
				}
				list.add(new UploadFile(fileItem));
			}
		}
	}

	public int paramInt(String paramname) {
		try {
			return Integer.parseInt(paramString(paramname));
		} catch (Exception e) {
			return 0;
		}
	}

	public int paramInt(String paramname, int defaultvalue) {
		try {
			return Integer.parseInt(paramString(paramname));
		} catch (Exception e) {
			return defaultvalue;
		}
	}

	public long paramLong(String paramname) {
		try {
			return Long.parseLong(paramString(paramname));
		} catch (Exception e) {
			return 0;
		}
	}

	public long paramLong(String paramname, long value) {
		try {
			return Long.parseLong(paramString(paramname));
		} catch (Exception e) {
			return value;
		}
	}

	public short paramShort(String paramname) {
		try {
			return Short.parseShort(paramString(paramname));
		} catch (Exception e) {
			return 0;
		}
	}

	public short paramShort(String paramname, short value) {
		try {
			return Short.parseShort(paramString(paramname));
		} catch (Exception e) {
			return value;
		}
	}

	public byte paramByte(String paramname) {
		try {
			return Byte.parseByte(paramString(paramname));
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public byte paramByte(String paramname, byte value) {
		try {
			return Byte.parseByte(paramString(paramname));
		} catch (Exception e) {
			return value;
		}
	}

	@Override
	public float paramFloat(String paramname, float value) {
		try {
			return Float.parseFloat(paramString(paramname));
		} catch (Exception e) {
			return value;
		}
	}

	@Override
	public float paramFloat(String paramname) {
		try {
			return Float.parseFloat(paramString(paramname));
		} catch (Exception e) {
			return 0.0f;
		}
	}

	@Override
	public double paramDouble(String paramname) {
		try {
			return Double.parseDouble(paramString(paramname));
		} catch (Exception e) {
			return 0.00d;
		}
	}

	@Override
	public double paramDouble(String paramname, double value) {
		try {
			return Double.parseDouble(paramString(paramname));
		} catch (Exception e) {
			return value;
		}
	}

	@Override
	public boolean paramBoolean(String paramname) {
		try {
			return Boolean.parseBoolean(paramString(paramname));
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String paramString(String paramname) {
		List<String> list = params.get(paramname);
		if (list != null)
			return list.get(0);
		return null;
	}

	@Override
	public String paramString(String paramname, String defaultvalue) {
		String value = paramString(paramname);
		if (value == null)
			return defaultvalue;
		return value;
	}

	@Override
	public Integer paramObjectInt(String paramname) {
		try {
			return Integer.parseInt(paramString(paramname));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Long paramObjectLong(String paramname) {
		try {
			return Long.parseLong(paramString(paramname));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Short paramObjectShort(String paramname) {
		try {
			return Short.parseShort(paramString(paramname));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Byte paramObjectByte(String paramname) {
		try {
			return Byte.parseByte(paramString(paramname));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Float paramObjectFloat(String paramname) {
		try {
			return Float.parseFloat(paramString(paramname));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Double paramObjectDouble(String paramname) {
		try {
			return Double.parseDouble(paramString(paramname));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String[] paramValues(String paramname) {
		List<String> list = params.get(paramname);
		if (list != null)
			return list.toArray(new String[0]);
		return null;
	}

	@Override
	public Map<String, String[]> paramMaps() {
		Map<String, String[]> map = new HashMap<>();
		Set<Entry<String, List<String>>> set = params.entrySet();
		for (Entry<String, List<String>> entry : set) {
			List<String> list = entry.getValue();
			if (list != null) {
				map.put(entry.getKey(), list.toArray(new String[0]));
			}
		}
		return map;
	}

	@Override
	public List<UploadFile> paramFiles(String paramname) {
		return fileParams.get(paramname);
	}

	@Override
	public UploadFile paramFile(String paramname) {
		List<UploadFile> files = fileParams.get(paramname);
		if (files != null)
			return files.get(0);
		return null;
	}

	@Override
	public void release() {
		super.release();
		params.clear();
		if (fileParams != null && !fileParams.isEmpty()) {
			Set<Entry<String, List<UploadFile>>> enset = fileParams.entrySet();
			for (Entry<String, List<UploadFile>> en : enset) {
				try {
					for (UploadFile uf : en.getValue()) {
						uf.delete();
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			fileParams.clear();
		}
	}
}
