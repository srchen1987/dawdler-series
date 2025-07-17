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
package club.dawdler.clientplug.dynamicform.control;

import club.dawdler.clientplug.velocity.ControlTag;

/**
 * @author jackson.song
 * @version V1.0
 * 抽象控件
 */
public abstract class Control {
	private static boolean showcolon;
	protected ControlTag tag;

	protected Control(ControlTag tag) {
		this.tag = tag;
	}

	public static void setShowcolon(boolean showcolon) {
		Control.showcolon = showcolon;
	}

	public String showView() {
		ControlTag ntag = tag;
		if (ntag.getControlName() == null) {
			throw new NullPointerException("controlName can't null !");
		}
		if (ntag.getViewName() == null) {
			throw new NullPointerException("viewName can't null !");
		}
		return translation();
	}

	protected abstract String replaceContent();

	protected String translation() {
		String notEmpty = "";
		if (tag.getValidateRule() != null) {
			if (tag.getValidateRule().contains("notEmpty")) {
				notEmpty = "<font color=\"red\">*</font>";
			}
		}
		return notEmpty + (tag.isAutoAddViewName() ? tag.getViewName() + (showcolon ? " : " : "  ") : "")
				+ replaceContent()
				+ ((tag.getViewDescription() != null && tag.isAutoAddViewDescription()) ? tag.getViewDescription()
						: "");
	}
}
