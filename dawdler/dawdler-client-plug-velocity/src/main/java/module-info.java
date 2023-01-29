import com.anywide.dawdler.clientplug.web.plugs.DisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.impl.VelocityDisplayPlug;

module dawdler.client.plug.velocity {
	requires java.base;
	requires dawdler.load.bean;
	requires dawdler.util;
	requires dawdler.client.plug;
	requires dawdler.jakarta.fileupload;
	requires org.slf4j;
	requires org.jsoup;
	requires jakarta.servlet;
	requires velocity.engine.core;

	uses DisplayPlug;

	provides DisplayPlug with VelocityDisplayPlug;

}