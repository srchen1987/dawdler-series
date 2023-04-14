import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.client.load.resource.ClientLoadLifeCycle;

module dawdler.server.plug {
	requires java.base;
	requires dawdler.util;
	requires dawdler.server;
	requires dawdler.client;
	requires dawdler.core;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires org.dom4j;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ClientLoadLifeCycle;
}