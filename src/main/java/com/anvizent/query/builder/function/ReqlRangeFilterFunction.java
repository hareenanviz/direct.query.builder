package com.anvizent.query.builder.function;

import java.time.ZoneOffset;
import java.util.ArrayList;

import com.anvizent.universal.query.json.RangeDetails;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.gen.ast.ReqlFunction1;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class ReqlRangeFilterFunction implements ReqlFunction1 {

	private ArrayList<RangeDetails> rangeDetails;

	public ReqlRangeFilterFunction(ArrayList<RangeDetails> rangeDetails, ZoneOffset timeZoneOffset) {
		this.rangeDetails = rangeDetails;
	}

	public Object apply(ReqlExpr initRow) {
		ReqlExpr reqlExpr;

		reqlExpr = applyRangeExpr(initRow);

		return reqlExpr;
	}

	private ReqlExpr applyRangeExpr(ReqlExpr initRow) {
		ReqlExpr reqlExpr;

		if (rangeDetails.get(0).getLowerBound() == null && rangeDetails.get(0).getUpperBound() == null) {
			return initRow;
		} else if (rangeDetails.get(0).getLowerBound() == null && rangeDetails.get(0).getUpperBound() != null) {
			reqlExpr = initRow.g(rangeDetails.get(0).getRangeField()).lt(rangeDetails.get(0).getUpperBound());
			reqlExpr = reqlExpr.or(initRow.g(rangeDetails.get(0).getRangeField()).eq(rangeDetails.get(0).getUpperBound()));
		} else if (rangeDetails.get(0).getLowerBound() != null && rangeDetails.get(0).getUpperBound() != null) {
			reqlExpr = initRow.g(rangeDetails.get(0).getRangeField()).gt(rangeDetails.get(0).getLowerBound());
			reqlExpr = reqlExpr.and(initRow.g(rangeDetails.get(0).getRangeField()).lt(rangeDetails.get(0).getUpperBound())
					.or(initRow.g(rangeDetails.get(0).getRangeField()).eq(rangeDetails.get(0).getUpperBound())));
		} else {
			reqlExpr = initRow.g(rangeDetails.get(0).getRangeField()).gt(rangeDetails.get(0).getLowerBound());
		}

		return reqlExpr;
	}

}
