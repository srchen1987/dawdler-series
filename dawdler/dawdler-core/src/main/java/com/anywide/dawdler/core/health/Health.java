/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.core.health;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author jackson.song
 * @version V1.0
 * @Title Health.java
 * @Description Health实体类
 * @date 2022年5月3日
 * @email suxuan696@gmail.com
 */
public class Health {

	@JsonIgnore
	private final String name;

	@JsonInclude(Include.NON_EMPTY)
	private final Map<String, Object> data;

	protected Health(String name, Builder builder) {
		this.name = name;
		builder.details.put("status", builder.status);
		this.data = Collections.unmodifiableMap(builder.details);
	}

	public String getStatus() {
		return (String) data.get("status");
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getData() {
		return this.data;
	}

	@Override
	public String toString() {
		return getStatus() + ":" + getData();
	}

	public static Builder unknown() {
		return status(Status.UNKNOWN);
	}

	public static Builder staring() {
		return status(Status.STARTING);
	}

	public static Builder outOfService() {
		return status(Status.OUT_OF_SERVICE);
	}

	public static Builder up() {
		return status(Status.UP);
	}

	public static Builder down(Throwable ex) {
		return down().withException(ex);
	}

	public static Builder down() {
		return status(Status.DOWN);
	}


	public static Builder status(String status) {
		return new Builder(status);
	}

	public static class Builder {

		private String status;

		private String name;

		private Map<String, Object> details;

		public Builder() {
			this.status = Status.UNKNOWN;
			this.details = new LinkedHashMap<>();
		}

		public Builder(String status) {
			this.status = status;
			this.details = new LinkedHashMap<>();
		}

		public Builder(String status, Map<String, ?> details) {
			this.status = status;
			this.details = new LinkedHashMap<>(details);
		}

		public Builder withException(Throwable ex) {
			return withDetail("error", ex.getClass().getName() + ": " + ex.getMessage());
		}

		public Builder withDetail(String key, Object value) {
			this.details.put(key, value);
			return this;
		}

		public Builder unknown() {
			return status(Status.UNKNOWN);
		}

		public Builder starting() {
			return status(Status.STARTING);
		}

		public Builder outOfService() {
			return status(Status.OUT_OF_SERVICE);
		}

		public Builder up() {
			return status(Status.UP);
		}

		public Builder down(Throwable ex) {
			return down().withException(ex);
		}

		public Builder down() {
			return status(Status.DOWN);
		}

		public Builder status(String status) {
			this.status = status;
			return this;
		}

		public Health build() {
			return new Health(name, this);
		}

		public String getName() {
			return name;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

	}

}
