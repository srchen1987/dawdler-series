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
package club.dawdler.schedule.shutdown;

import java.io.Closeable;

import club.dawdler.core.shutdown.ContainerGracefulShutdown;
import club.dawdler.schedule.ScheduleOperator;

/**
 * @author jackson.song
 * @version V1.0
 * Schedule优雅停机 等待任务完成之后调用closeable.close(); 
 */
public class ScheduleGracefulShutdown implements ContainerGracefulShutdown {

	@Override
	public void shutdown(Closeable closeable) throws Exception {
		if(closeable != null) {
			ScheduleOperator.shutdown();
			ScheduleOperator.waitAll();
			closeable.close();
		}else {
			ScheduleOperator.shutdownNow();
		}
	}

}
