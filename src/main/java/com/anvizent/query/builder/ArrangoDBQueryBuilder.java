package com.anvizent.query.builder;

import java.text.ParseException;

import com.anvizent.query.builder.exception.InvalidInputException;
import com.anvizent.query.builder.exception.UnderConstructionException;
import com.anvizent.query.builder.service.ArangoDBQueryBuilderService;
import com.anvizent.universal.query.json.Pagination;
import com.anvizent.universal.query.json.UniversalQueryJson;
import com.arangodb.ArangoDB;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class ArrangoDBQueryBuilder extends QueryBuilder {

	private ArangoDBQueryBuilderService arangoDBQueryBuilderService = new ArangoDBQueryBuilderService();

	@Override
	public Object build(UniversalQueryJson universalQueryJson) throws InvalidInputException, UnderConstructionException, ParseException {
		validate(universalQueryJson);
		return arangoDBQueryBuilderService.build(universalQueryJson);
	}

	private void validate(UniversalQueryJson universalQueryJson) throws InvalidInputException {
		if (universalQueryJson == null) {
			throw new InvalidInputException("Input is null");
		}

		if (universalQueryJson.getTableName() == null || universalQueryJson.getTableName().isEmpty()) {
			throw new InvalidInputException("Table name is not provided");
		}

		validatePagination(universalQueryJson.getPagination());

		// TODO validations
	}

	private void validatePagination(Pagination pagination) throws InvalidInputException {
		if (pagination != null) {
			if (pagination.getCurrentPage() == null) {
				throw new InvalidInputException("Pagination.currentPage is not provided");
			}

			if (pagination.getPageSize() == null) {
				throw new InvalidInputException("Pagination.pageSize is not provided");
			}
		}
	}

	@Override
	public Object execute(Object query, UniversalQueryJson universalQueryJson, Object connection, Class<?> returnType) throws InvalidInputException {
		if (query == null) {
			throw new InvalidInputException("Query cannot be null");
		} else if (universalQueryJson == null) {
			throw new InvalidInputException("Universal query json cannot be null");
		} else if (universalQueryJson.getDbName() == null || universalQueryJson.getDbName().isEmpty()) {
			throw new InvalidInputException("DB name cannot be null");
		} else if (connection == null) {
			throw new InvalidInputException("Connection cannot be null");
		} else {
			return ((ArangoDB) connection).db(universalQueryJson.getDbName()).query((String) query, null, null, returnType);
		}
	}

}
