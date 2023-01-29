package com.anywide.dawdler.serverplug.db.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.serverplug.db.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.serverplug.db.transaction.TransactionServiceExecutor;
/**
 * @author jackson.song
 * @version V1.0
 * @Title TransactionLifeCycle.java
 * @Description 绑定事务管理器(代替老版本中的PlugInit)
 * @date 2022年4月30日
 * @email suxuan696@gmail.com
 */
@Order(2)
public class TransactionLifeCycle implements ComponentLifeCycle {
	private static final Logger logger = LoggerFactory.getLogger(TransactionLifeCycle.class);

	@Override
	public void prepareInit() throws Throwable {
		try {
			DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
			dawdlerContext.setAttribute(RWSplittingDataSourceManager.DATASOURCE_MANAGER_PREFIX,
					new RWSplittingDataSourceManager(dawdlerContext));
			dawdlerContext.setAttribute(ServiceBase.SERVICE_EXECUTOR_PREFIX, new TransactionServiceExecutor());
		} catch (Throwable e) {
			logger.error("", e);
		}
	}
}