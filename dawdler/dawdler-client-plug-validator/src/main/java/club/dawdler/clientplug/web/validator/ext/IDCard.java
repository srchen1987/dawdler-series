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
package club.dawdler.clientplug.web.validator.ext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jackson.song
 * @version V1.0
 * 身份证验证
 */
public class IDCard {
	private static final Map<String, String> HASHTABLE = new HashMap<>();

	private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]*");

	private static final Pattern DATE_PATTERN = Pattern.compile(
			"^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");

	static {
		HASHTABLE.put("11", "北京");
		HASHTABLE.put("12", "天津");
		HASHTABLE.put("13", "河北");
		HASHTABLE.put("14", "山西");
		HASHTABLE.put("15", "内蒙古");
		HASHTABLE.put("21", "辽宁");
		HASHTABLE.put("22", "吉林");
		HASHTABLE.put("23", "黑龙江");
		HASHTABLE.put("31", "上海");
		HASHTABLE.put("32", "江苏");
		HASHTABLE.put("33", "浙江");
		HASHTABLE.put("34", "安徽");
		HASHTABLE.put("35", "福建");
		HASHTABLE.put("36", "江西");
		HASHTABLE.put("37", "山东");
		HASHTABLE.put("41", "河南");
		HASHTABLE.put("42", "湖北");
		HASHTABLE.put("43", "湖南");
		HASHTABLE.put("44", "广东");
		HASHTABLE.put("45", "广西");
		HASHTABLE.put("46", "海南");
		HASHTABLE.put("50", "重庆");
		HASHTABLE.put("51", "四川");
		HASHTABLE.put("52", "贵州");
		HASHTABLE.put("53", "云南");
		HASHTABLE.put("54", "西藏");
		HASHTABLE.put("61", "陕西");
		HASHTABLE.put("62", "甘肃");
		HASHTABLE.put("63", "青海");
		HASHTABLE.put("64", "宁夏");
		HASHTABLE.put("65", "新疆");
		HASHTABLE.put("71", "台湾");
		HASHTABLE.put("81", "香港");
		HASHTABLE.put("82", "澳门");
		HASHTABLE.put("91", "国外");
	}

	/**
	 * 1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，
	 * 三位数字顺序码和一位数字校验码。
	 *
	 * 2、地址码(前六位数） 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。
	 *
	 * 3、出生日期码（第七位至十四位） 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。
	 *
	 * 4、顺序码（第十五位至十七位） 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配给女性。
	 *
	 * 5、校验码（第十八位数） （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16
	 * ，先对前17位数字的权求和 Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7
	 * 9 10 5 8 4 2 （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10
	 * 校验码: 1 0 X 9 8 7 6 5 4 3 2
	 *
	 * 所以我们就可以大致写一个函数来校验是否正确了。
	 */

	/**
	 * ======================================================================
	 * 功能：身份证的有效验证
	 *
	 * @param IDStr 身份证号
	 * @return 有效：true 无效：false
	 * @throws ParseException
	 */
	public static boolean IDCardValidate(String IDStr) throws ParseException {
		IDStr = IDStr.toLowerCase();
//		String errorInfo = "";// 记录错误信息
		String[] ValCodeArr = { "1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2" };
		String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2" };
		// String[] Checker = {"1","9","8","7","6","5","4","3","2","1","1"};
		String Ai = "";

		// ================ 号码的长度 15位或18位 ================
		if (IDStr.length() != 15 && IDStr.length() != 18) {
//			errorInfo = "号码长度应该为15位或18位。";
			return false;
		}
		// =======================(end)========================

		// ================ 数字 除最后以为都为数字 ================
		if (IDStr.length() == 18) {
			Ai = IDStr.substring(0, 17);
		} else if (IDStr.length() == 15) {
			Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
		}
		if (isNumeric(Ai) == false) {
//			errorInfo = "15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
			return false;
		}
		// =======================(end)========================

		// ================ 出生年月是否有效 ================
		String strYear = Ai.substring(6, 10);// 年份
		String strMonth = Ai.substring(10, 12);// 月份
		String strDay = Ai.substring(12, 14);// 日子

		if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) {
//			errorInfo = "生日无效。";
			return false;
		}

		GregorianCalendar gc = new GregorianCalendar();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
				|| (gc.getTime().getTime() - s.parse(strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
//			errorInfo = "生日不在有效范围。";
			return false;
		}
		if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
//			errorInfo = "月份无效";
			return false;
		}
		if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
//			errorInfo = "日期无效";
			return false;
		}
		// =====================(end)=====================

		// ================ 地区码时候有效 ================
		if (HASHTABLE.get(Ai.substring(0, 2)) == null) {
//			errorInfo = "地区编码错误。";
			return false;
		}
		// ==============================================

		// ================ 判断最后一位的值 ================
		int TotalmulAiWi = 0;
		for (int i = 0; i < 17; i++) {
			TotalmulAiWi = TotalmulAiWi + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
		}
		int modValue = TotalmulAiWi % 11;
		String strVerifyCode = ValCodeArr[modValue];
		Ai = Ai + strVerifyCode;

		if (IDStr.length() == 18) {
			if (Ai.equals(IDStr) == false) {
//				errorInfo = "身份证无效，最后一位字母错误";
				return false;
			}
		} else {
			return true;
		}
		// =====================(end)=====================
		return true;
	}

	/**
	 * ======================================================================
	 * 功能：判断字符串是否为数字
	 *
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) {
		Matcher isNum = NUMBER_PATTERN.matcher(str);
		return isNum.matches();
		/*
		 * 判断一个字符时候为数字 if(Character.isDigit(str.charAt(0))) { return true; } else {
		 * return false; }
		 */
	}

	/**
	 * ======================================================================
	 * 功能：判断字符串是否为日期格式
	 *
	 * @param strDate
	 * @return
	 */
	public static boolean isDate(String strDate) {
		Matcher m = DATE_PATTERN.matcher(strDate);
		return m.matches();
	}

}
