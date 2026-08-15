import club.dawdler.web.gateway.route.filter.GatewayPathFilter;
import club.dawdler.web.gateway.route.filter.RewritePathFilter;
import club.dawdler.web.gateway.route.filter.SetPathFilter;
import club.dawdler.web.gateway.route.filter.StripPrefixFilter;

module dawdler.web.gateway {
	requires dawdler.util;
	requires org.slf4j;
	requires io.vertx.core;
	requires com.fasterxml.jackson.annotation;
	requires jakarta.servlet;

	exports club.dawdler.web.gateway.config;
	exports club.dawdler.web.gateway.filter;
	exports club.dawdler.web.gateway.handler;
	exports club.dawdler.web.gateway.route;
	exports club.dawdler.web.gateway.route.filter;

	uses GatewayPathFilter;

	provides GatewayPathFilter with StripPrefixFilter, RewritePathFilter, SetPathFilter;
}
