package com.anywide.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.NewService.Check;

public class TestConsulServiceCreate {
	public static void main(String[] args) {
		ConsulClient client = new ConsulClient("localhost", 8500);

		NewService service = new NewService();
		String ipAddress = "192.168.43.138";
		int port = 8500;
		service.setAddress(ipAddress);
		service.setName("user-service");
		service.setPort(port);
		service.setId("user-service:" + ipAddress + ":" + port);
		Check check = new Check();
		check.setHttp("http://" + ipAddress + ":19001/status");
		Map<String, List<String>> header = new HashMap<>();
		List<String> list = new ArrayList();
		list.add("Basic amFja3NvbjpqYWNrc29uLnNvbmc=");
		header.put("Authorization", list);
		check.setHeader(header);
		check.setInterval("1s");
//		System.out.println(CheckStatus.CRITICAL.PASSING.name())8;
		check.setStatus("warning");
		service.setCheck(check);
		client.agentServiceRegister(service);
//		check.setInterval("10s");

//		QueryParams qp = QueryParams.Builder.builder().build();
//		client.getHealthChecksState(qp);

//		client.agentServiceDeregister("");

	}

}
