package com.anywide.dawdler.client.conf;

import static com.anywide.dawdler.util.XmlObject.getElementAttribute;
import static com.anywide.dawdler.util.XmlObject.getElementAttribute2Int;

import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;
public class Test {
	public static void main(String[] args) throws DocumentException, IOException {
		String xmlPath = DawdlerTool.getcurrentPath() + "client/client-conf.xml";
		
		XmlObject xmlo = new XmlObject(xmlPath);
		Element root = xmlo.getRoot();
		System.out.println(root);
		ClientConfig config = new ClientConfig();
		Node zkHost = root.selectSingleNode("zk-host");
		if(zkHost != null) {
			config.setZkHost(zkHost.getText());
		}
		
		Node certificatePath = root.selectSingleNode("certificatePath");
		if(certificatePath != null) {
			config.setCertificatePath(certificatePath.getText());
		}
		
		List<Node>  serverChannelGroupNode = root.selectNodes("server-channel-group");
		for(Node node : serverChannelGroupNode) {
			ServerChannelGroup serverChannelGroup = config.new ServerChannelGroup();
			
			Element serverChannelGroupEle = (Element) node;
			String groupId = getElementAttribute(serverChannelGroupEle, "channel-group-id");
			String path = getElementAttribute(serverChannelGroupEle, "service-path");
			int connectionNum = getElementAttribute2Int(serverChannelGroupEle, "connection-num", 2);
			int sessionNum = getElementAttribute2Int(serverChannelGroupEle, "session-num", 2);
			int serializer = getElementAttribute2Int(serverChannelGroupEle, "serializer", 2);
			String user = getElementAttribute(serverChannelGroupEle, "user");
			String password = getElementAttribute(serverChannelGroupEle, "password");
			
			serverChannelGroup.setGroupId(groupId);
			serverChannelGroup.setPath(path);
			serverChannelGroup.setConnectionNum(connectionNum);
			serverChannelGroup.setSessionNum(sessionNum);
			serverChannelGroup.setSerializer(serializer);
			serverChannelGroup.setUser(user);
			serverChannelGroup.setPassword(password);
			
			config.getServerChannelGroups().add(serverChannelGroup);
		}
		
	}

}
