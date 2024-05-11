import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.jedis.resource.JedisLifeCycle;

module dawdler.server.plug.jedis {
	requires java.base;
	requires dawdler.core;
	requires dawdler.server;
	requires dawdler.jedis.core;
	requires dawdler.service.core;

	uses ComponentLifeCycle;
	provides ComponentLifeCycle with JedisLifeCycle;
}