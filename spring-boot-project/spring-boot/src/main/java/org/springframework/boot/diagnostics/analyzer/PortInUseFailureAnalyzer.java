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

package org.springframework.boot.diagnostics.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.web.server.PortInUseException;

/**
 * A {@code FailureAnalyzer} that performs analysis of failures caused by a
 * {@code PortInUseException}.
 *
 * @author Andy Wilkinson
 */
class PortInUseFailureAnalyzer extends AbstractFailureAnalyzer<PortInUseException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, PortInUseException cause) {
		return new FailureAnalysis(new StringBuilder().append("Web server failed to start. Port ").append(cause.getPort()).append(" was already in use.").toString(),
				new StringBuilder().append("Identify and stop the process that's listening on port ").append(cause.getPort()).append(" or configure this ").append("application to listen on another port.").toString(),
				cause);
	}

}
