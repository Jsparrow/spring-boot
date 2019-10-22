/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint.web.documentation;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for generating documentation describing {@link ThreadDumpEndpoint}.
 *
 * @author Andy Wilkinson
 */
class ThreadDumpEndpointDocumentationTests extends MockMvcEndpointDocumentationTests {

	private static final Logger logger = LoggerFactory.getLogger(ThreadDumpEndpointDocumentationTests.class);

	@Test
	void jsonThreadDump() throws Exception {
		ReentrantLock lock = new ReentrantLock();
		CountDownLatch latch = new CountDownLatch(1);
		new Thread(() -> {
			try {
				lock.lock();
				try {
					latch.await();
				}
				finally {
					lock.unlock();
				}
			}
			catch (InterruptedException ex) {
				logger.error(ex.getMessage(), ex);
				Thread.currentThread().interrupt();
			}
		}).start();
		this.mockMvc.perform(get("/actuator/threaddump").accept(MediaType.APPLICATION_JSON)).andExpect(
				status().isOk()).andDo(
						MockMvcRestDocumentation
								.document("threaddump/json", preprocessResponse(limit("threads")),
										responseFields(fieldWithPath("threads").description("JVM's threads."),
												fieldWithPath("threads.[].blockedCount").description(
														"Total number of times that the thread has been blocked."),
												fieldWithPath("threads.[].blockedTime").description(
														new StringBuilder().append("Time in milliseconds that the thread has spent ").append("blocked. -1 if thread contention ").append("monitoring is disabled.").toString()),
												fieldWithPath("threads.[].daemon")
														.description("Whether the thread is a daemon "
																+ "thread. Only available on Java 9 or later.")
														.optional().type(JsonFieldType.BOOLEAN),
												fieldWithPath("threads.[].inNative")
														.description("Whether the thread is executing native code."),
												fieldWithPath("threads.[].lockName")
														.description("Description of the object on which the "
																+ "thread is blocked, if any.")
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].lockInfo")
														.description("Object for which the thread is blocked waiting.")
														.optional().type(JsonFieldType.OBJECT),
												fieldWithPath("threads.[].lockInfo.className")
														.description("Fully qualified class name of the lock object.")
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].lockInfo.identityHashCode")
														.description("Identity hash code of the lock object.")
														.optional().type(JsonFieldType.NUMBER),
												fieldWithPath("threads.[].lockedMonitors")
														.description("Monitors locked by this thread, if any"),
												fieldWithPath("threads.[].lockedMonitors.[].className")
														.description("Class name of the lock object.").optional()
														.type(JsonFieldType.STRING),
												fieldWithPath("threads.[].lockedMonitors.[].identityHashCode")
														.description("Identity hash code of the lock object.")
														.optional().type(JsonFieldType.NUMBER),
												fieldWithPath("threads.[].lockedMonitors.[].lockedStackDepth")
														.description("Stack depth where the monitor was locked.")
														.optional().type(JsonFieldType.NUMBER),
												subsectionWithPath("threads.[].lockedMonitors.[].lockedStackFrame")
														.description("Stack frame that locked the monitor.").optional()
														.type(JsonFieldType.OBJECT),
												fieldWithPath("threads.[].lockedSynchronizers")
														.description("Synchronizers locked by this thread."),
												fieldWithPath("threads.[].lockedSynchronizers.[].className")
														.description("Class name of the locked synchronizer.")
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].lockedSynchronizers.[].identityHashCode")
														.description("Identity hash code of the locked synchronizer.")
														.optional().type(JsonFieldType.NUMBER),
												fieldWithPath("threads.[].lockOwnerId")
														.description(new StringBuilder().append("ID of the thread that owns the object on which ").append("the thread is blocked. `-1` if the ").append("thread is not blocked.").toString()),
												fieldWithPath("threads.[].lockOwnerName")
														.description("Name of the thread that owns the "
																+ "object on which the thread is blocked, if any.")
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].priority")
														.description("Priority of the thread. Only "
																+ "available on Java 9 or later.")
														.optional().type(JsonFieldType.NUMBER),
												fieldWithPath("threads.[].stackTrace")
														.description("Stack trace of the thread."),
												fieldWithPath("threads.[].stackTrace.[].classLoaderName")
														.description(new StringBuilder().append("Name of the class loader of the ").append("class that contains the execution ").append("point identified by this entry, if ").append("any. Only available on Java 9 or later.")
																.toString())
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].stackTrace.[].className")
														.description("Name of the class that contains the "
																+ "execution point identified by this entry."),
												fieldWithPath("threads.[].stackTrace.[].fileName")
														.description(new StringBuilder().append("Name of the source file that ").append("contains the execution point ").append("identified by this entry, if any.").toString())
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].stackTrace.[].lineNumber")
														.description(new StringBuilder().append("Line number of the execution ").append("point identified by this entry. ").append("Negative if unknown.").toString()),
												fieldWithPath("threads.[].stackTrace.[].methodName")
														.description("Name of the method."),
												fieldWithPath("threads.[].stackTrace.[].moduleName")
														.description(new StringBuilder().append("Name of the module that contains ").append("the execution point identified by ").append("this entry, if any. Only available ").append("on Java 9 or later.")
																.toString())
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].stackTrace.[].moduleVersion")
														.description(new StringBuilder().append("Version of the module that ").append("contains the execution point ").append("identified by this entry, if any. ").append("Only available on Java 9 or later.")
																.toString())
														.optional().type(JsonFieldType.STRING),
												fieldWithPath("threads.[].stackTrace.[].nativeMethod")
														.description("Whether the execution point is a native method."),
												fieldWithPath("threads.[].suspended")
														.description("Whether the thread is suspended."),
												fieldWithPath("threads.[].threadId").description("ID of the thread."),
												fieldWithPath("threads.[].threadName")
														.description("Name of the thread."),
												fieldWithPath("threads.[].threadState")
														.description(new StringBuilder().append("State of the thread (").append(describeEnumValues(Thread.State.class)).append(").")
																.toString()),
												fieldWithPath("threads.[].waitedCount")
														.description("Total number of times that the thread has waited"
																+ " for notification."),
												fieldWithPath("threads.[].waitedTime")
														.description(new StringBuilder().append("Time in milliseconds that the thread has spent ").append("waiting. -1 if thread contention ").append("monitoring is disabled").toString()))));
		latch.countDown();
	}

	@Test
	void textThreadDump() throws Exception {
		this.mockMvc.perform(get("/actuator/threaddump").accept(MediaType.TEXT_PLAIN)).andExpect(status().isOk())
				.andDo(MockMvcRestDocumentation.document("threaddump/text",
						preprocessResponse(new ContentModifyingOperationPreprocessor((bytes, mediaType) -> {
							String content = new String(bytes, StandardCharsets.UTF_8);
							return content.substring(0, content.indexOf("\"main\" - Thread")).getBytes();
						}))));
	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseDocumentationConfiguration.class)
	static class TestConfiguration {

		@Bean
		ThreadDumpEndpoint endpoint() {
			return new ThreadDumpEndpoint();
		}

	}

}
