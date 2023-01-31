package com.anywide.test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServiceRequest;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;

public class TestConsulService {
	public static void main(String[] args) throws InterruptedException {
		ConsulClient client = new ConsulClient("localhost", 8500);

//		NewService service = new NewService();

//		service.setAddress("192.168.43.137");
//		service.setName("user-service");
//		service.setPort(8500);
//		service.setId("user-service");
//		
//		Check check = new Check();
//		check.setHttp("http://192.168.43.137:19200/status");
//		check.setInterval("10s");
//		
//		service.setCheck(check);

//		client.agentServiceRegister(service);
		Set<String> connectionSet = new HashSet<>();

		new Thread(() -> {
			long lastIndex = -1;
			while (true) {
				System.out.println("lastIndex：:" + lastIndex);
				System.out.println("query start date :" + new Date());
				CatalogServiceRequest cr = CatalogServiceRequest.newBuilder()
						.setQueryParams(new QueryParams(30, lastIndex)).build();
				Response<List<CatalogService>> response = client.getCatalogService("user-service", cr);
				System.out.println("query over date :" + new Date());
				long currentLastIndex = response.getConsulIndex();
				System.out.println(currentLastIndex + ":" + lastIndex);
				if (currentLastIndex != lastIndex) {
					Map<String, CatalogService> cache = response.getValue().stream()
							.collect(Collectors.toMap(CatalogService::getServiceId, catalogService -> catalogService));
//				HealthChecksForServiceRequest hr = HealthChecksForServiceRequest.newBuilder().build();
//				Response<List<com.ecwid.consul.v1.health.model.Check>> listCheck = client.getHealthChecksForService("user-service", hr);
//				listCheck.getValue().forEach((c)->{
//					System.out.println(c.getServiceName()+":"+c.getStatus());
//					System.out.println(c.getNode());
//					if(c.getStatus()==CheckStatus.CRITICAL) {
//						System.out.println("critical");
//						try {
//							client.agentServiceDeregister(c.getServiceId());
//						} catch (Exception e) {
//						}
//					}
//					if(c.getStatus()==CheckStatus.PASSING) {
//						System.out.println("PASSING");
//						System.out.println("ServiceId:"+cache.get(c.getServiceId()).getAddress()+":");
//					}
//				});
//				 cache.values().forEach(c->{
//					System.out.println(c.getAddress()+":"+c.getServicePort());
//				});
					lastIndex = currentLastIndex;
				}
			}
		});// .start();

		new Thread(() -> {
			long lastIndex = -1;
			while (true) {
				System.out.println("health lastIndex：:" + lastIndex);
				HealthChecksForServiceRequest hr = HealthChecksForServiceRequest.newBuilder()
						.setQueryParams(new QueryParams(30, lastIndex)).build();
				System.out.println("health start date :" + new Date());
				Response<List<com.ecwid.consul.v1.health.model.Check>> listCheck = client
						.getHealthChecksForService("user-service", hr);
				long currentLastIndex = listCheck.getConsulIndex();
				System.out.println("health over date :" + new Date());
				CatalogServiceRequest cr = CatalogServiceRequest.newBuilder().build();
				Response<List<CatalogService>> response = client.getCatalogService("user-service", cr);
				Map<String, CatalogService> cache = response.getValue().stream()
						.collect(Collectors.toMap(CatalogService::getServiceId, catalogService -> catalogService));

				listCheck.getValue().forEach((c) -> {
					System.out.println(c.getServiceName() + ":" + c.getStatus());
					System.out.println(c.getNode());
					if (c.getStatus() == CheckStatus.CRITICAL) {
						System.out.println("critical:" + c.getServiceId());
						try {
							client.agentServiceDeregister(c.getServiceId());
						} catch (Exception e) {
						}
					}
					if (c.getStatus() == CheckStatus.PASSING) {
						System.out.println("PASSING");
					}
				});
				lastIndex = currentLastIndex;
			}
		}).start();

//		response.getValue().forEach((c)->{
//			System.out.println(c.getTaggedAddresses());
//			System.out.println(c.getServiceId()+"\t"+c.getServiceName());
//			System.out.println(c.getServiceAddress());
//			System.out.println(c.getNodeMeta());
//			System.out.println(c.getServiceMeta());
//			System.out.println(c);
//		});
//		for(int i = 0;i<1;i++) {
////			Thread.sleep(20);
//			new Thread(()->{
//				long lastIndex = -1;
//				System.out.println("lastIndex:"+lastIndex);
//				while(true) {
//					HealthChecksForServiceRequest hr = HealthChecksForServiceRequest.newBuilder().setQueryParams(new QueryParams(10, lastIndex)).build();
//					System.out.println(hr);
//					Response<List<com.ecwid.consul.v1.health.model.Check>> listCheck = client.getHealthChecksForService("user-service", hr);
//					lastIndex = listCheck.getConsulIndex();
//					System.out.println(lastIndex);
//					listCheck.getValue().forEach((c)->{
//						System.out.println(c.getServiceName()+":"+c.getStatus());
//						System.out.println(c.getNode());
//						if(c.getStatus()==CheckStatus.CRITICAL) {
//							System.out.println("critical");
//							try {
//								client.agentServiceDeregister(c.getServiceId());
//							} catch (Exception e) {
//							}
//						}
//						if(c.getStatus()==CheckStatus.PASSING) {
//							System.out.println("PASSING");
//							
//							
////							c.getServiceId()
//						}
//					});
//				
//				}
//			}).start();
//		}

//		QueryParams qp = QueryParams.Builder.builder().build();
//		client.getHealthChecksState(qp);

//		client.agentServiceDeregister("");

	}

}
