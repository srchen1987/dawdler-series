import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.plugin.dawdler.DawdlerPlugin;
import com.navercorp.pinpoint.plugin.dawdler.DawdlerTraceMetadataProvider;

module dawdler.pinpoint.plug{
	uses ProfilerPlugin;
	provides ProfilerPlugin with DawdlerPlugin;
	uses TraceMetadataProvider;
	provides TraceMetadataProvider with DawdlerTraceMetadataProvider;
	requires java.base;
	requires dawdler.client;
	requires pinpoint.bootstrap.core;
	requires pinpoint.commons;
	requires dawdler.server;
}