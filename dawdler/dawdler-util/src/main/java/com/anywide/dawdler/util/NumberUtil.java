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
import java.nio.ByteOrder;


/**
 * 
 * @Title:  NumberUtil.java
 * @Description:    数字位数转换类   
 * @author: jackson.song    
 * @date:   2014年08月12日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class NumberUtil {
	public static byte[] intToByte(int i){
		return intToByte(i, ByteOrder.nativeOrder());
	}
	public static int bytesToInt(byte[] bytes) {
		return bytesToInt(bytes, ByteOrder.nativeOrder());
	}
	public static byte[] intToByte(int i,ByteOrder byteOrder) {
		byte[] abyte0 = new byte[4];
		if(byteOrder==ByteOrder.BIG_ENDIAN){
			abyte0[0] = (byte) (0xff & i);
			abyte0[1] = (byte) ((0xff00 & i) >> 8);
			abyte0[2] = (byte) ((0xff0000 & i) >> 16);
			abyte0[3] = (byte) ((0xff000000 & i) >> 24);
		}else{
			abyte0[3] = (byte) (0xff & i);
			abyte0[2] = (byte) ((0xff00 & i) >> 8);
			abyte0[1] = (byte) ((0xff0000 & i) >> 16);
			abyte0[0] = (byte) ((0xff000000 & i) >> 24);
		}
		
		return abyte0;
	}
	public static int bytesToInt(byte[] bytes,ByteOrder byteOrder) {
		int addr = 0;
		if(byteOrder==ByteOrder.BIG_ENDIAN){
			addr= bytes[0] & 0xFF;
			addr |= ((bytes[1] << 8) & 0xFF00);
			addr |= ((bytes[2] << 16) & 0xFF0000);
			addr |= ((bytes[3] << 24) & 0xFF000000);
		}else{
			addr = bytes[3] & 0xFF;
			addr |= ((bytes[2] << 8) & 0xFF00);
			addr |= ((bytes[1] << 16) & 0xFF0000);
			addr |= ((bytes[0] << 24) & 0xFF000000);
		}
		return addr;
	}
}
