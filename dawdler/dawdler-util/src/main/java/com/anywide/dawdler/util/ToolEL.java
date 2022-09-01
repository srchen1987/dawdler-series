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
package com.anywide.dawdler.util;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ToolEL.java
 * @Description 通过反射实现的简易版EL语言，只建议在配置中使用
 * @date 2006年3月26日
 * @email suxuan696@gmail.com
 */
public class ToolEL {
	private static final byte BASETYPE = 0;
	private static final byte STRINGTYPE = 1;
	private static final byte LISTTYPE = 2;
	private static final byte MAPTYPE = 3;
	private static final byte SETYPE = 4;
	private static final byte BYTETYPE = 5;
	private static final byte SHORTTYPE = 6;
	private static final byte INTTYPE = 7;
	private static final byte LONGTYPE = 8;
	private static final byte FLOATTYPE = 9;
	private static final byte DOUBLETYPE = 10;
	private static final byte CHARTYPE = 11;
	private static final byte BOOLEANTYPE = 12;
	private static final byte OBJECTTYPE = 13;
	private static final byte ENUETYPE = 14;
	private static Pattern p = Pattern.compile("(.*)\\[(.*)\\]");

	private static Object getValue(Object o, String pname) {
		Field field = null;
		try {
			field = o.getClass().getDeclaredField(pname);
			field.setAccessible(true);
		} catch (Exception e) {
			return null;
		}
		try {
			return field.get(o);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return null;
	}

	public static Object getBeanValue(Object o, String path) {
		String[] tem = path.split("\\.");
		for (String s : tem) {
			String[] pa = getData(s);
			if (pa.length == 1) {
				o = getValue(o, pa[0]);
				if (o == null) {
					return null;
				}
			} else {
				if (!"".equals(pa[0].trim())) {
					o = getValue(o, pa[0]);
				}
				if (o == null) {
					return null;
				}
				int i = 0;
				String key = null;
				try {
					i = Integer.parseInt(pa[1]);
				} catch (Exception e) {
					key = pa[1];
				}
				switch (getType(o)) {
				case STRINGTYPE:
					try {
						o = ((String[]) o)[i];
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case BYTETYPE:
					try {
						try {
							o = ((byte[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Byte[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case SHORTTYPE:
					try {
						try {
							o = ((short[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Short[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case INTTYPE:
					try {
						try {
							o = ((int[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Integer[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case LONGTYPE:
					try {
						try {
							o = ((long[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Long[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case FLOATTYPE:
					try {
						try {
							o = ((float[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Float[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case CHARTYPE:
					try {
						try {
							o = ((char[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Character[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case BOOLEANTYPE:
					try {
						try {
							o = ((boolean[]) o)[i];
						} catch (ClassCastException e) {
							o = ((Boolean[]) o)[i];
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case LISTTYPE:
					try {
						o = ((List) o).get(i);
					} catch (IndexOutOfBoundsException e) {
						return null;
					}
					break;
				case SETYPE:
					try {
						o = ((Set) o).toArray()[i];
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					break;
				case ENUETYPE: {
					int j = 0;
					boolean b = false;
					Enumeration em = (Enumeration) o;
					while (em.hasMoreElements()) {
						if (j == i) {
							o = em.nextElement();
							b = true;
							break;
						} else {
							em.nextElement();
						}
						j++;
					}
					if (!b) {
						o = null;
					}
					break;
				}
				case MAPTYPE:
					if (key != null)
						o = ((Map) o).get(key);
					else {
						boolean b = false;
						int j = 0;
						for (Iterator it = (((Map) o)).values().iterator(); it.hasNext();) {
							Object temp = it.next();
							if (j == i) {
								b = true;
								o = temp;
								break;
							}
							j++;
						}
						if (!b) {
							o = null;
						}
					}
					break;
				default:
					o = ((Object[]) o)[i];
					break;
				}
			}
		}
		return o;
	}

	private static String[] getData(String s) {
		Matcher match = p.matcher(s);
		if (match.find()) {
			String[] tem = new String[2];
			tem[0] = match.group(1);
			tem[1] = match.group(2);
			return tem;
		}
		return new String[] { s };
	}

	private static int getType(Object o) {
		Class<?> type = o.getClass();
		if (type.isArray()) {
			if (type == String[].class) {
				return STRINGTYPE;
			}
			if (type == byte[].class || type == Byte[].class) {
				return BYTETYPE;
			}
			if (type == short[].class || type == Short[].class) {
				return SHORTTYPE;
			}
			if (type == int[].class || type == Integer[].class) {
				return INTTYPE;
			}
			if (type == long[].class || type == Long[].class) {
				return LONGTYPE;
			}
			if (type == float[].class || type == Float[].class) {
				return FLOATTYPE;
			}
			if (type == double[].class || type == Double[].class) {
				return DOUBLETYPE;
			}
			if (type == char[].class || type == Character[].class) {
				return CHARTYPE;
			}
			if (type == boolean[].class || type == Boolean[].class) {
				return BOOLEANTYPE;
			}
			return OBJECTTYPE;
		}
		if (o instanceof List) {
			return LISTTYPE;
		}
		if (o instanceof Map) {
			return MAPTYPE;
		}
		if (o instanceof Set) {
			return SETYPE;
		}
		if (o instanceof Enumeration) {
			return ENUETYPE;
		}
		return BASETYPE;
	}
}
