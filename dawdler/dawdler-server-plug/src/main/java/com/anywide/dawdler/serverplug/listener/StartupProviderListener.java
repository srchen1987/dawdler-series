package com.anywide.dawdler.serverplug.listener;

import org.dom4j.Element;
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
	private static final Logger logger = LoggerFactory.getLogger(StartupProviderListener.class);
	private DiscoveryCenter discoveryCenter;

	@Override
	public void contextInitialized(DawdlerContext dawdlerContext) throws Exception {
		Element root = XmlConfig.getConfig().getRoot();
		Element zkHost = (Element) root.selectSingleNode("zk-host");
		if (zkHost != null) {
			String username = zkHost.attributeValue("username");
			String password = zkHost.attributeValue("password");
			String url = zkHost.getTextTrim();
			String channelGroup = dawdlerContext.getDeployName();
			if ("".equals(url))
				throw new NullPointerException("zk-host can't be null!");
			discoveryCenter = new ZkDiscoveryCenter(url, username, password);
			discoveryCenter.init();
			String path = channelGroup + "/" + dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
			discoveryCenter.addProvider(path, dawdlerContext.getHost() + ":" + dawdlerContext.getPort());
			dawdlerContext.setAttribute(DiscoveryCenter.class, discoveryCenter);
		} else {
			logger.error("not find discoveryServer config!");
		}

	}

	@Override
	public void contextDestroyed(DawdlerContext dawdlerContext) throws Exception {
		if (discoveryCenter != null)
			discoveryCenter.destroy();
	}
}
