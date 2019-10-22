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

package smoketest.kafka;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
class Consumer {

	private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
	private final List<SampleMessage> messages = new CopyOnWriteArrayList<>();

	@KafkaListener(topics = "testTopic")
	void processMessage(SampleMessage message) {
		this.messages.add(message);
		logger.info(new StringBuilder().append("Received sample message [").append(message).append("]").toString());
	}

	List<SampleMessage> getMessages() {
		return this.messages;
	}

}
