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

package smoketest.jooq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import static smoketest.jooq.domain.Author.AUTHOR;
import static smoketest.jooq.domain.Book.BOOK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JooqExamples implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(JooqExamples.class);

	private final DSLContext dsl;

	private final JdbcTemplate jdbc;

	public JooqExamples(DSLContext dsl, JdbcTemplate jdbc) {
		this.dsl = dsl;
		this.jdbc = jdbc;
	}

	@Override
	public void run(String... args) throws Exception {
		jooqFetch();
		jooqSql();
	}

	private void jooqFetch() {
		Result<Record> results = this.dsl.select().from(AUTHOR).fetch();
		for (Record result : results) {
			Integer id = result.getValue(AUTHOR.ID);
			String firstName = result.getValue(AUTHOR.FIRST_NAME);
			String lastName = result.getValue(AUTHOR.LAST_NAME);
			logger.info(new StringBuilder().append("jOOQ Fetch ").append(id).append(" ").append(firstName).append(" ").append(lastName)
					.toString());
		}
	}

	private void jooqSql() {
		Query query = this.dsl.select(BOOK.TITLE, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME).from(BOOK).join(AUTHOR)
				.on(BOOK.AUTHOR_ID.equal(AUTHOR.ID)).where(BOOK.PUBLISHED_IN.equal(2015));
		Object[] bind = query.getBindValues().toArray(new Object[0]);
		List<String> list = this.jdbc.query(query.getSQL(), bind, (ResultSet rs, int rowNum) -> new StringBuilder().append(rs.getString(1)).append(" : ").append(rs.getString(2)).append(" ").append(rs.getString(3)).toString());
		logger.info("jOOQ SQL " + list);
	}

}
