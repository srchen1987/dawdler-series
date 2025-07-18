import club.dawdler.clientplug.web.validator.injector.ValidatorInjector;
import club.dawdler.core.component.injector.CustomComponentInjector;

module dawdler.client.plug.validator {
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires dawdler.core;

	exports club.dawdler.clientplug.web.validator;
	exports club.dawdler.clientplug.web.validator.operators;
	exports club.dawdler.clientplug.web.validator.entity;
	exports club.dawdler.clientplug.web.validator.webbind;
	exports club.dawdler.clientplug.web.validator.exception;

	uses CustomComponentInjector;

	provides CustomComponentInjector with ValidatorInjector;
}
