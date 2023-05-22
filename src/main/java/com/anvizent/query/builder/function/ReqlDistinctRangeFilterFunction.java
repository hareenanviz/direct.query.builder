package com.anvizent.query.builder.function;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.anvizent.universal.query.json.DistinctRangeDetails;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.gen.ast.ReqlFunction1;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class ReqlDistinctRangeFilterFunction implements ReqlFunction1 {

	private ArrayList<DistinctRangeDetails> distinctRangeDetails;

	public ReqlDistinctRangeFilterFunction(ArrayList<DistinctRangeDetails> distinctRangeDetails, ZoneOffset timeZoneOffset) {
		this.distinctRangeDetails = distinctRangeDetails;
	}

	public Object apply(ReqlExpr initRow) {
		ReqlExpr reqlExpr;

		reqlExpr = applyRangeExpr(initRow);

		return reqlExpr;
	}

	private ReqlExpr applyRangeExpr(ReqlExpr initRow) {
		ReqlExpr reqlExpr = null;

		ArrayList<Map<String, Object>> distinctDataList = distinctRangeDetails.get(0).getDistinctDataList();

		for (Map<String, Object> record : distinctDataList) {
			ReqlExpr recordReqlExpr = null;
			for (Entry<String, Object> fieldWithValue : record.entrySet()) {
				if (recordReqlExpr == null) {
					recordReqlExpr = initRow.g(fieldWithValue.getKey()).eq(fieldWithValue.getValue());
				} else {
					recordReqlExpr = recordReqlExpr.and(initRow.g(fieldWithValue.getKey()).eq(fieldWithValue.getValue()));
				}
			}

			if (reqlExpr == null) {
				reqlExpr = recordReqlExpr;
			} else {
				reqlExpr = reqlExpr.or(recordReqlExpr);
			}
		}

		if (reqlExpr == null) {
			reqlExpr = initRow;
		}

		return reqlExpr;
	}

}
