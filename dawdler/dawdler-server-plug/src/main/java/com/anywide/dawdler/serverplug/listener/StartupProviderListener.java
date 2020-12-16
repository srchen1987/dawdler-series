package com.anywide.dawdler.serverplug.listener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.core.discoverycenter.ZkDiscoveryCenter;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;
import com.anywide.dawdler.serverplug.util.XmlConfig;

@Order(Integer.MAX_VALUE)
public class StartupProviderListener implements DawdlerServiceListener {
	private static Logger logger = LoggerFactory.getLogger(StartupProviderListener.class);
	private DiscoveryCenter discoveryCenter;

	@Override
	public void contextDestroyed(DawdlerContext dawdlerContext) throws Exception {
		if(discoveryCenter != null)
			discoveryCenter.destroy();
	}

	@Override
	public void contextInitialized(DawdlerContext dawdlerContext) throws Exception {
		Map data = XmlConfig.getDatas().get("discoveryServer");
		if (data != null) {
			if ("zk".equals(data.get("type"))) {
				String url = (String) data.get("url");
				String channelGroup = (String) data.get("channel-group-id");
				if (channelGroup == null || channelGroup.trim().equals(""))
					channelGroup = "defaultgroup";
				if (url == null)
					throw new NullPointerException("zk url can't be null!");
				discoveryCenter = new ZkDiscoveryCenter(url, null, null);
				discoveryCenter.init();
				String path = channelGroup + "/" + dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
				discoveryCenter.addProvider(path, dawdlerContext.getHost() + ":" + dawdlerContext.getPort());
				dawdlerContext.setAttribute(DiscoveryCenter.class, discoveryCenter);
			}
		} else {
			logger.error("not find discoveryServer config!");
		}

	}

}
