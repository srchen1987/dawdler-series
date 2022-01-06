module dawdler.client.plug.validator {
	exports com.anywide.dawdler.clientplug.web.validator;
	exports com.anywide.dawdler.clientplug.web.validator.entity;
	exports com.anywide.dawdler.clientplug.web.validator.webbind;
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires dom4j;
}