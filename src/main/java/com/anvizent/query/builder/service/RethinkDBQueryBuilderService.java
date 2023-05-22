package com.anvizent.query.builder.service;

import com.anvizent.query.builder.exception.UnderConstructionException;
import com.anvizent.query.builder.function.ReqlDistinctRangeFilterFunction;
import com.anvizent.query.builder.function.ReqlFilterFunction;
import com.anvizent.query.builder.function.ReqlRangeFilterFunction;
import com.anvizent.universal.query.json.Pagination;
import com.anvizent.universal.query.json.UniversalQueryJson;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.ReqlExpr;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class RethinkDBQueryBuilderService {

	public Object build(UniversalQueryJson universalQueryJson) throws UnderConstructionException {
		ReqlExpr reqlExpr;

		reqlExpr = buildSelectAllQuery(universalQueryJson.getTableName());

		if (universalQueryJson.getGroupByClause() == null) {
			reqlExpr = buildSelectQuery(reqlExpr, universalQueryJson);
		} else if (universalQueryJson.getGroupByClause() != null) {
			reqlExpr = buildGroupByQuery(reqlExpr, universalQueryJson);
		} else {
			throw new UnderConstructionException();
		}

		reqlExpr = buildSlice(reqlExpr, universalQueryJson);
		reqlExpr = buildCount(reqlExpr, universalQueryJson);

		return reqlExpr;
	}

	private ReqlExpr buildSelectQuery(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) throws UnderConstructionException {
		ReqlExpr reqlExpr;

		reqlExpr = buildWhereClause(initExpr, universalQueryJson);
		reqlExpr = buildV2WhereClause(initExpr, universalQueryJson);
		reqlExpr = buildSelectFieldsQuery(reqlExpr, universalQueryJson);
		reqlExpr = buildLimitQuery(reqlExpr, universalQueryJson);

		return reqlExpr;
	}

	private ReqlExpr buildV2WhereClause(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) {
		if (universalQueryJson.getWhereClauseV2() != null) {
			ReqlExpr reqlExpr = initExpr.filter(new ReqlFilterFunction(universalQueryJson.getWhereClauseV2(), universalQueryJson.getTimeZoneOffset()));

			return reqlExpr;
		} else {
			return initExpr;
		}
	}

	private ReqlExpr buildSlice(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) {
		if (universalQueryJson.getPagination() != null) {
			ReqlExpr reqlExpr = initExpr.slice(getFromSlice(universalQueryJson.getPagination()), getToSlice(universalQueryJson.getPagination()));

			return reqlExpr;
		} else {
			return initExpr;
		}
	}

	private Object getToSlice(Pagination pagination) {
		if (pagination.getCurrentPage() == 0) {
			return pagination.getPageSize();
		} else {
			return (pagination.getCurrentPage() + 1) * pagination.getPageSize();
		}
	}

	private Object getFromSlice(Pagination pagination) {
		if (pagination.getCurrentPage() == 0) {
			return 0L;
		} else {
			return (pagination.getPageSize() * pagination.getCurrentPage());
		}
	}

	private ReqlExpr buildGroupByQuery(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) {
		if (universalQueryJson.getGroupByClause().getGroupByFields() != null && !universalQueryJson.getGroupByClause().getGroupByFields().isEmpty()) {
			ReqlExpr reqlExpr = initExpr.group(rRow -> rRow.pluck(universalQueryJson.getGroupByClause().getGroupByFields())).count();

			return reqlExpr;
		} else {
			return initExpr;
		}
	}

	private ReqlExpr buildSelectAllQuery(String tableName) {
		return RethinkDB.r.table(tableName);
	}

	private ReqlExpr buildWhereClause(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) throws UnderConstructionException {
		if (universalQueryJson.getWhereClause() != null) {
			if (universalQueryJson.getWhereClause().getRangeDetails() != null && !universalQueryJson.getWhereClause().getRangeDetails().isEmpty()) {
				ReqlExpr reqlExpr = initExpr
						.filter(new ReqlRangeFilterFunction(universalQueryJson.getWhereClause().getRangeDetails(), universalQueryJson.getTimeZoneOffset()));

				return reqlExpr;
			} else if (universalQueryJson.getWhereClause().getDistinctRangeDetails() != null
					&& !universalQueryJson.getWhereClause().getDistinctRangeDetails().isEmpty()) {
				ReqlExpr reqlExpr = initExpr.filter(new ReqlDistinctRangeFilterFunction(universalQueryJson.getWhereClause().getDistinctRangeDetails(),
						universalQueryJson.getTimeZoneOffset()));

				return reqlExpr;
			} else {
				return initExpr;
			}
		} else {
			return initExpr;
		}
	}

	private ReqlExpr buildSelectFieldsQuery(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) {
		if (universalQueryJson.getSelectClause() != null
				&& (universalQueryJson.getSelectClause().getSelectFields() != null && !universalQueryJson.getSelectClause().getSelectFields().isEmpty())) {
			ReqlExpr reqlExpr = initExpr.pluck(universalQueryJson.getSelectClause().getSelectFields());

			return reqlExpr;
		} else {
			return initExpr;
		}
	}

	private ReqlExpr buildLimitQuery(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) {
		if (universalQueryJson.getLimit() != null) {
			ReqlExpr reqlExpr = initExpr.limit(universalQueryJson.getLimit());

			return reqlExpr;
		} else {
			return initExpr;
		}
	}

	private ReqlExpr buildCount(ReqlExpr initExpr, UniversalQueryJson universalQueryJson) {
		if (universalQueryJson.isCount()) {
			ReqlExpr reqlExpr = initExpr.count();

			return reqlExpr;
		} else {
			return initExpr;
		}
	}
}
