package com.anvizent.query.builder;

import com.anvizent.query.builder.exception.InvalidInputException;
import com.anvizent.universal.query.json.SourceType;
import com.anvizent.universal.query.json.UniversalQueryJson;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public abstract class QueryBuilder {

	public static final Object getQueryBuilder(SourceType sourceType) {
		if (sourceType == null) {
			return null;
		} else {
			switch (sourceType) {
				case RETHINK_DB:
					return new RethinkDBQueryBuilder();
				case ARANGO_DB:
					return new ArrangoDBQueryBuilder();
				default:
					return null;
			}
		}
	}

	public abstract Object build(UniversalQueryJson universalQueryJson) throws Exception;

	public abstract Object execute(Object query, UniversalQueryJson universalQueryJson, Object connection, Class<?> returnType) throws InvalidInputException;

}
