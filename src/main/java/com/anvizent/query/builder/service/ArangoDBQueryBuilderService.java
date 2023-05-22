package com.anvizent.query.builder.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.anvizent.query.builder.exception.UnderConstructionException;
import com.anvizent.universal.query.json.GroupByClause;
import com.anvizent.universal.query.json.Pagination;
import com.anvizent.universal.query.json.SelectClause;
import com.anvizent.universal.query.json.UniversalQueryJson;
import com.anvizent.universal.query.json.v2.Condition;
import com.anvizent.universal.query.json.v2.ConditionType;
import com.anvizent.universal.query.json.v2.WhereClause;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class ArangoDBQueryBuilderService {

	public Object build(UniversalQueryJson universalQueryJson) throws UnderConstructionException, ParseException {
		if (isTotalCount(universalQueryJson)) {
			return "RETURN LENGTH(" + universalQueryJson.getTableName() + ")";
		} else {
			String fromClause = getFromClause(universalQueryJson.getTableName());
			String groupByClause = getGroupByClause(universalQueryJson.getGroupByClause(), universalQueryJson);
			String selectClause = getSelectClause(universalQueryJson.getSelectClause(), universalQueryJson);
			String whereClause = getWhereClause(universalQueryJson.getWhereClauseV2());
			String limitClause = getLimitClause(universalQueryJson.getLimit(), universalQueryJson.getPagination());
			return fromClause + " " + groupByClause + " " + whereClause + " " + limitClause + " " + selectClause;
		}
	}

	private boolean isTotalCount(UniversalQueryJson universalQueryJson) {
		return universalQueryJson.isCount() && (universalQueryJson.getCountAlias() == null || universalQueryJson.getCountAlias().isEmpty())
		        && universalQueryJson.isSelectClauseEmpty() && universalQueryJson.isWhereClauseV2Empty() && universalQueryJson.isGroupByEmpty();
	}

	private String getLimitClause(BigDecimal limit, Pagination pagination) {
		if (pagination != null && pagination.getCurrentPage() != null && pagination.getPageSize() != null) {
			return "LIMIT " + pagination.getCurrentPage() + ", " + pagination.getPageSize();
		} else if (limit != null) {
			return "LIMIT " + limit.longValueExact();
		} else {
			return "";
		}
	}

	private String getGroupByClause(GroupByClause groupByClause, UniversalQueryJson universalQueryJson) {
		String count = "";
		if (universalQueryJson.isCount()) {
			String countAlias = "_count";
			if (universalQueryJson.getCountAlias() != null && !universalQueryJson.getCountAlias().isEmpty()) {
				countAlias = universalQueryJson.getCountAlias();
			}
			count = " WITH COUNT INTO " + countAlias;

			universalQueryJson.getSelectClause().getSelectFields().add(countAlias);
		}

		String groupByFields = "";
		if (!universalQueryJson.isGroupByEmpty()) {
			for (String groupByField : universalQueryJson.getGroupByClause().getGroupByFields()) {
				if (!groupByFields.isEmpty()) {
					groupByFields += ", ";
				}

				groupByFields += groupByField + " = o." + groupByField;
			}
		}

		String groupByClauseQuery = "";
		if (!groupByFields.isEmpty()) {
			groupByClauseQuery = "COLLECT " + groupByFields;
		}

		if (!count.isEmpty()) {
			if (groupByClauseQuery.isEmpty()) {
				groupByClauseQuery = "COLLECT";
			}

			groupByClauseQuery += count;
		}

		return groupByClauseQuery;
	}

	public static void main(String[] args) {
		System.out.println(Date.class.getName());
	}

	private String getWhereClause(WhereClause whereClause) throws ParseException, UnderConstructionException {
		String filter = getWhereClauseCondition(whereClause);
		if (filter == null || filter.isEmpty()) {
			return "";
		} else {
			return "FILTER " + filter;
		}
	}

	private String getWhereClauseCondition(WhereClause whereClause) throws ParseException, UnderConstructionException {
		if (whereClause == null) {
			return "";
		} else if (whereClause.getAnd() != null && !whereClause.getAnd().isEmpty()) {
			String filter = "";
			for (WhereClause andClause : whereClause.getAnd()) {
				if (!filter.isEmpty()) {
					filter += " && ";
				}
				filter += getWhereClauseCondition(andClause);
			}

			return filter;
		} else if (whereClause.getOr() != null && !whereClause.getOr().isEmpty()) {
			String filter = "";
			for (WhereClause orClause : whereClause.getOr()) {
				if (!filter.isEmpty()) {
					filter += " || ";
				}
				filter += getWhereClauseCondition(orClause);
			}

			return filter;
		} else if (whereClause.getCondition() == null || whereClause.getCondition().getField() == null || whereClause.getCondition().getField().isEmpty()
		        || whereClause.getCondition().getType() == null || whereClause.getCondition().getType().isEmpty()) {
			return "";
		} else {
			if (whereClause.getCondition().getType().equalsIgnoreCase(ConditionType.EQ.name())) {
				if (isString(whereClause.getCondition())) {
					return "o." + whereClause.getCondition().getField() + " == \"" + whereClause.getCondition().getValue() + "\"";
				} else if (isNumber(whereClause.getCondition())) {
					return "o." + whereClause.getCondition().getField() + " == " + whereClause.getCondition().getValue();
				} else if (isDate(whereClause.getCondition())) {
					String value;
					if (whereClause.getCondition().getDateFormat() == null || whereClause.getCondition().getDateFormat().isEmpty()) {
						value = whereClause.getCondition().getValue();
					} else {
						value = "" + new SimpleDateFormat(whereClause.getCondition().getDateFormat()).parse(whereClause.getCondition().getValue()).getTime();
					}

					return "o." + whereClause.getCondition().getField() + " == " + value;
				} else {
					throw new UnderConstructionException("Condition field type '" + whereClause.getCondition().getDataTypeClass() + "' is not implemented.");
				}
			} else if (whereClause.getCondition().getType().equalsIgnoreCase(ConditionType.REG_EX.name())) {
				return "REGEX_TEST(o." + whereClause.getCondition().getField() + ", " + whereClause.getCondition().getValue() + ")";
			} else {
				throw new UnderConstructionException("Condition type '" + whereClause.getCondition().getType() + "' is not implemented.");
			}
		}
	}

	private boolean isDate(Condition condition) {
		if ((condition.getDataTypeClass() != null && condition.getDataType() != null || condition.getDataType() == null)) {
			return condition.getDataTypeClass().equals(Date.class);
		} else {
			return condition.getDataType().equals(Date.class.getName());
		}
	}

	private boolean isNumber(Condition condition) {
		if ((condition.getDataTypeClass() != null && condition.getDataType() != null || condition.getDataType() == null)) {
			return condition.getDataTypeClass().equals(Byte.class) || condition.getDataTypeClass().equals(Short.class)
			        || condition.getDataTypeClass().equals(Integer.class) || condition.getDataTypeClass().equals(Long.class)
			        || condition.getDataTypeClass().equals(Float.class) || condition.getDataTypeClass().equals(Double.class)
			        || condition.getDataTypeClass().equals(BigDecimal.class) || condition.getDataTypeClass().equals(Boolean.class);
		} else {
			return condition.getDataTypeClass().equals(Byte.class) || condition.getDataTypeClass().equals(Short.class)
			        || condition.getDataTypeClass().equals(Integer.class) || condition.getDataTypeClass().equals(Long.class)
			        || condition.getDataTypeClass().equals(Float.class) || condition.getDataTypeClass().equals(Double.class)
			        || condition.getDataTypeClass().equals(BigDecimal.class) || condition.getDataTypeClass().equals(Boolean.class);
		}
	}

	private boolean isString(Condition condition) {
		if (condition.getDataTypeClass() == null && condition.getDataType() == null) {
			return true;
		} else if ((condition.getDataTypeClass() != null && condition.getDataType() != null || condition.getDataType() == null)) {
			return condition.getDataTypeClass().equals(String.class) || condition.getDataTypeClass().equals(Character.class);
		} else {
			return condition.getDataType().equals(String.class.getName()) || condition.getDataType().equals(Character.class.getName());
		}
	}

	private String getSelectClause(SelectClause selectClause, UniversalQueryJson universalQueryJson) {
		if (selectClause == null || selectClause.getSelectFields() == null || selectClause.getSelectFields().isEmpty()) {
			return "RETURN o";
		} else {
			String quote = (universalQueryJson.getCountAlias() == null || universalQueryJson.getCountAlias().isEmpty()) && universalQueryJson.isGroupByEmpty()
			        ? ""
			        : "\"";
			String fields = "";
			for (String selectField : selectClause.getSelectFields()) {
				if (!fields.isEmpty()) {
					fields += ", ";
				}

				fields += quote + selectField + quote + ": o." + selectField;
			}

			return "RETURN { " + fields + " }";
		}
	}

	private String getFromClause(String tableName) {
		return "FOR o IN " + tableName;
	}
}
