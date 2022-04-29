import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.redis.resource.JedisLifeCycle;

module dawdler.server.plug.redis {
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with JedisLifeCycle;
	requires java.base;
	requires dawdler.core;
	requires dawdler.server.plug;
	requires dawdler.redis.core;
	requires dawdler.server;
}