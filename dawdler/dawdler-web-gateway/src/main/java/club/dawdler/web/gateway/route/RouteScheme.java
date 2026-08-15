/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.web.gateway.route;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 路由协议枚举，定义网关支持的目标 URI scheme。
 */
public enum RouteScheme {

	LB("lb"),
	LB_WS("lb:ws"),
	LB_WSS("lb:wss"),
	LB_HTTP("lb:http"),
	LB_HTTPS("lb:https"),
	HTTP("http"),
	HTTPS("https"),
	WS("ws"),
	WSS("wss"),
	FORWARD("forward");

	private final String scheme;

	RouteScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getScheme() {
		return scheme;
	}

	public boolean isLb() {
		return this == LB || this == LB_WS || this == LB_WSS || this == LB_HTTP || this == LB_HTTPS;
	}

	public boolean isWebSocket() {
		return this == WS || this == WSS || this == LB_WS || this == LB_WSS;
	}

	public boolean isSecure() {
		return this == HTTPS || this == WSS || this == LB_HTTPS || this == LB_WSS;
	}

	public static RouteScheme fromUri(String uri) {
		if (uri == null) {
			return null;
		}
		if (uri.startsWith("lb:ws://")) {
			return LB_WS;
		}
		if (uri.startsWith("lb:wss://")) {
			return LB_WSS;
		}
		if (uri.startsWith("lb:http://")) {
			return LB_HTTP;
		}
		if (uri.startsWith("lb:https://")) {
			return LB_HTTPS;
		}
		if (uri.startsWith("lb://")) {
			return LB;
		}
		if (uri.startsWith("forward://")) {
			return FORWARD;
		}
		if (uri.startsWith("wss://")) {
			return WSS;
		}
		if (uri.startsWith("ws://")) {
			return WS;
		}
		if (uri.startsWith("https://")) {
			return HTTPS;
		}
		if (uri.startsWith("http://")) {
			return HTTP;
		}
		return null;
	}

	public static String extractServiceName(String uri) {
		if (uri == null) {
			return null;
		}
		if (uri.startsWith("lb:ws://")) {
			return uri.substring("lb:ws://".length());
		}
		if (uri.startsWith("lb:wss://")) {
			return uri.substring("lb:wss://".length());
		}
		if (uri.startsWith("lb:http://")) {
			return uri.substring("lb:http://".length());
		}
		if (uri.startsWith("lb:https://")) {
			return uri.substring("lb:https://".length());
		}
		if (uri.startsWith("lb://")) {
			return uri.substring("lb://".length());
		}
		return null;
	}

	public static String resolveDownstreamProtocol(RouteScheme scheme) {
		if (scheme == null) {
			return null;
		}
		switch (scheme) {
			case LB:
				return "http";
			case LB_WS:
				return "ws";
			case LB_WSS:
				return "wss";
			case LB_HTTP:
				return "http";
			case LB_HTTPS:
				return "https";
			default:
				return null;
		}
	}

}
