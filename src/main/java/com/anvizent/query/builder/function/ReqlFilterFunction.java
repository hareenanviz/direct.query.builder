package com.anvizent.query.builder.function;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

import com.anvizent.query.builder.common.util.TypeConversionUtil;
import com.anvizent.query.builder.exception.DateParseException;
import com.anvizent.query.builder.exception.InvalidInputException;
import com.anvizent.query.builder.exception.InvalidSituationException;
import com.anvizent.query.builder.exception.UnsupportedCoerceException;
import com.anvizent.universal.query.json.v2.Condition;
import com.anvizent.universal.query.json.v2.ConditionType;
import com.anvizent.universal.query.json.v2.WhereClause;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.gen.ast.ReqlFunction1;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class ReqlFilterFunction implements ReqlFunction1 {

	private WhereClause whereClause;
	private ZoneOffset timeZoneOffset;;

	public ReqlFilterFunction(WhereClause whereClause, ZoneOffset timeZoneOffset) {
		this.whereClause = whereClause;
		this.timeZoneOffset = timeZoneOffset;
	}

	public Object apply(ReqlExpr initRow) {
		try {
			ReqlExpr reqlExpr;

			reqlExpr = applyExpr(initRow, whereClause);

			return reqlExpr;
		} catch (UnsupportedCoerceException | InvalidSituationException | DateParseException | InvalidInputException exception) {
			throw new RuntimeException(exception.getMessage(), exception);
		}
	}

	private ReqlExpr applyExpr(ReqlExpr initRow, WhereClause whereClause)
			throws UnsupportedCoerceException, InvalidSituationException, DateParseException, InvalidInputException {
		ReqlExpr reqlExpr = null;

		if (whereClause.getCondition() != null) {
			reqlExpr = applyCondition(initRow, whereClause.getCondition());
		} else if (whereClause.getAnd() != null && !whereClause.getAnd().isEmpty()) {
			reqlExpr = applyAND(initRow, whereClause.getAnd());
		} else if (whereClause.getOr() != null && !whereClause.getOr().isEmpty()) {
			reqlExpr = applyOR(initRow, whereClause.getOr());
		} else {
			reqlExpr = initRow;
		}

		return reqlExpr;
	}

	private ReqlExpr applyCondition(ReqlExpr initRow, Condition condition) throws UnsupportedCoerceException, InvalidSituationException, DateParseException {
		ReqlExpr reqlConditionExpr = null;

		if (condition.getType().equalsIgnoreCase(ConditionType.EQ.getValue())) {
			reqlConditionExpr = initRow.g(condition.getField()).eq(getValue(condition.getValue(), condition.getDataTypeClass(), condition.getDateFormat()));
		} else if (condition.getType().equalsIgnoreCase(ConditionType.REG_EX.getValue())) {
			reqlConditionExpr = initRow.g(condition.getField()).match(getValue(condition.getValue(), condition.getDataTypeClass(), condition.getDateFormat()));
		} else {
			reqlConditionExpr = initRow;
		}

		return reqlConditionExpr;
	}

	private ReqlExpr applyAND(ReqlExpr initRow, ArrayList<WhereClause> and)
			throws UnsupportedCoerceException, InvalidSituationException, DateParseException, InvalidInputException {
		if (and.size() < 2) {
			throw new InvalidInputException("More than one 'Where clause' is required for 'and' condition.");
		} else {
			ReqlExpr reqlANDExpr = null;

			reqlANDExpr = applyExpr(initRow, and.get(0));

			for (int i = 1; i < and.size(); i++) {
				reqlANDExpr = reqlANDExpr.and(applyExpr(initRow, and.get(i)));
			}

			return reqlANDExpr;
		}
	}

	private ReqlExpr applyOR(ReqlExpr initRow, ArrayList<WhereClause> or)
			throws UnsupportedCoerceException, InvalidSituationException, DateParseException, InvalidInputException {
		if (or.size() < 2) {
			throw new InvalidInputException("More than one 'Where clause' is required for 'or' condition.");
		} else {
			ReqlExpr reqlORExpr = null;

			reqlORExpr = applyExpr(initRow, or.get(0));

			for (int i = 1; i < or.size(); i++) {
				reqlORExpr = reqlORExpr.or(applyExpr(initRow, or.get(i)));
			}

			return reqlORExpr;
		}
	}

	@SuppressWarnings("rawtypes")
	private Object getValue(String fromField, Class toType, String toFormat) throws UnsupportedCoerceException, InvalidSituationException, DateParseException {
		Object value = TypeConversionUtil.stringToOtherTypeConversion(fromField, toType, toFormat);

		if (value != null && value.getClass().equals(Date.class)) {
			OffsetDateTime offsetDateTime = (OffsetDateTime) TypeConversionUtil.dateToOffsetDateTypeConversion(value, Date.class, OffsetDateTime.class,
					timeZoneOffset);
			return offsetDateTime;
		} else {
			return value;
		}
	}
}
