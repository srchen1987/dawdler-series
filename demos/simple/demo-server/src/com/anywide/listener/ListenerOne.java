package com.anywide.listener;
import javax.naming.NamingException;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;
import com.anywide.dawdler.serverplug.transaction.LocalConnectionFacotry;
/**
 * 
监听器需要实现DawdlerServiceListener接口 监听器可以注入service与controller注入方式一致
*
 */
public class ListenerOne implements DawdlerServiceListener{
	@Override
	public void contextDestroyed(DawdlerContext dawdlerContext) {
	}

	@Override
	public void contextInitialized(DawdlerContext dawdlerContext) {
		System.out.println("demo_one启动。。。。");
	try {
		//获取数据源
		System.out.println("---new----"+LocalConnectionFacotry.getDataSourceInDawdler("read1"));
	} catch (NamingException e) {
		e.printStackTrace();
	}
	}
}

