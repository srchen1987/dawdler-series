import com.anywide.dawdler.clientplug.web.validator.injector.ValidatorInjector;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;

module dawdler.client.plug.validator {
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires dawdler.core;

	exports com.anywide.dawdler.clientplug.web.validator;
	exports com.anywide.dawdler.clientplug.web.validator.operators;
	exports com.anywide.dawdler.clientplug.web.validator.entity;
	exports com.anywide.dawdler.clientplug.web.validator.webbind;
	exports com.anywide.dawdler.clientplug.web.validator.exception;

	uses CustomComponentInjector;

	provides CustomComponentInjector with ValidatorInjector;
}