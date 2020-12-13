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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @Title: JsonWiewForward.java
 * @Description: json类型获取数据的一种实现，无用了 在楼兰为了兼容前端请求的格式而做
 * @author: jackson.song
 * @date: 2013年04月18日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
@Deprecated
public class JsonWiewForward extends ViewForward {
	public JsonWiewForward(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public int paramInt(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Integer.parseInt(o.toString());
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return super.paramInt(paramname);
	}

	@Override
	public int paramInt(String paramname, int defaultvalue) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Integer.parseInt(o.toString());
				} catch (Exception e) {
					return defaultvalue;
				}
			}
		}
		return super.paramInt(paramname, defaultvalue);
	}

	@Override
	public long paramLong(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Long.parseLong(o.toString());
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return super.paramLong(paramname);
	}

	@Override
	public long paramLong(String paramname, long value) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Long.parseLong(o.toString());
				} catch (Exception e) {
					return value;
				}
			}
		}
		return super.paramLong(paramname, value);
	}

	@Override
	public short paramShort(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Short.parseShort(o.toString());
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return super.paramShort(paramname);
	}

	@Override
	public short paramShort(String paramname, short value) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Short.parseShort(o.toString());
				} catch (Exception e) {
					return value;
				}
			}
		}
		return super.paramShort(paramname, value);
	}

	@Override
	public byte paramByte(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Byte.parseByte(o.toString());
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return super.paramByte(paramname);
	}

	@Override
	public byte paramByte(String paramname, byte value) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Byte.parseByte(o.toString());
				} catch (Exception e) {
					return value;
				}
			}
		}

		return super.paramByte(paramname, value);
	}

	@Override
	public float paramFloat(String paramname, float value) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Float.parseFloat(o.toString());
				} catch (Exception e) {
					return value;
				}
			}
		}
		return super.paramFloat(paramname, value);
	}

	@Override
	public float paramFloat(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Float.parseFloat(o.toString());
				} catch (Exception e) {
					return 0.00f;
				}
			}
		}
		return super.paramFloat(paramname);
	}

	@Override
	public double paramDouble(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Double.parseDouble(o.toString());
				} catch (Exception e) {
					return 0.00d;
				}
			}
		}
		return super.paramDouble(paramname);
	}

	@Override
	public double paramDouble(String paramname, double value) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Double.parseDouble(o.toString());
				} catch (Exception e) {
					return value;
				}
			}
		}
		return super.paramDouble(paramname, value);
	}

	@Override
	public boolean paramBoolean(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				try {
					return Boolean.parseBoolean(o.toString());
				} catch (Exception e) {
					return false;
				}
			}
		}
		return super.paramBoolean(paramname);
	}

	@Override
	public String paramString(String paramname) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				return o.toString();
			}
		}
		return super.paramString(paramname);
	}

	@Override
	public String paramString(String paramname, String defaultvalue) {
		if (jsonParams != null) {
			Object o = jsonParams.get(paramname);
			if (o != null) {
				return o.toString();
			}
		}
		return super.paramString(paramname, defaultvalue);
	}

	private Map<String, Object> jsonParams;

	public Map<String, Object> getJsonParams() {
		return jsonParams;
	}

	public void setJsonParams(Map<String, Object> jsonParams) {
		this.jsonParams = jsonParams;
	}
}
