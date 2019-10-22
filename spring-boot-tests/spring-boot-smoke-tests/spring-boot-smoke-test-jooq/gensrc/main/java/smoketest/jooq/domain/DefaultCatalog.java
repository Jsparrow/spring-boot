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

/**
 * This class is generated by jOOQ
 */
package smoketest.jooq.domain;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Schema;
import org.jooq.impl.CatalogImpl;
import java.util.Collections;

/**
 * This class is generated by jOOQ.
 */
@Generated(value = { "https://www.jooq.org",
		"jOOQ version:3.8.2" }, comments = "This class is generated by jOOQ")
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DefaultCatalog extends CatalogImpl {

	private static final long serialVersionUID = -1557925562;

	/**
	 * The reference instance of <code></code>
	 */
	public static final DefaultCatalog DEFAULT_CATALOG = new DefaultCatalog();

	/**
	 * The schema <code>PUBLIC</code>.
	 */
	public final Public PUBLIC = Public.PUBLIC;

	/**
	 * No further instances allowed
	 */
	private DefaultCatalog() {
		super("");
	}

	@Override
	public final List<Schema> getSchemas() {
		List result = new ArrayList();
		result.addAll(getSchemas0());
		return result;
	}

	private final List<Schema> getSchemas0() {
		return Collections.<Schema>singletonList(Public.PUBLIC);
	}

}
