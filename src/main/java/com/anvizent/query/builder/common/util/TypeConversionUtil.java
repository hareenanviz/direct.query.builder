package com.anvizent.query.builder.common.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import com.anvizent.query.builder.exception.DateParseException;
import com.anvizent.query.builder.exception.InvalidSituationException;
import com.anvizent.query.builder.exception.UnsupportedCoerceException;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
@SuppressWarnings("rawtypes")
public class TypeConversionUtil {

	public static Object stringToOtherTypeConversion(String fromFieldValue, Class coerceToType, String coerceToFormat)
			throws UnsupportedCoerceException, InvalidSituationException, DateParseException {
		if (fromFieldValue == null) {
			return null;
		} else if (coerceToType.equals(Byte.class)) {
			return Byte.valueOf(fromFieldValue);
		} else if (coerceToType.equals(Short.class)) {
			return Short.valueOf(fromFieldValue);
		} else if (coerceToType.equals(Character.class)) {
			if (fromFieldValue.length() == 1) {
				return (Character) fromFieldValue.charAt(0);
			} else {
				throw new InvalidSituationException("Invalid situation. Cannot convert from java.lang.String to " + coerceToType.getName());
			}
		} else if (coerceToType.equals(Integer.class)) {
			try {
				return Integer.valueOf(fromFieldValue);
			} catch (NumberFormatException exception) {
				throw new InvalidSituationException("Invalid situation. Cannot convert from java.lang.String to " + coerceToType.getName());
			}
		} else if (coerceToType.equals(Long.class)) {
			return Long.valueOf(fromFieldValue);
		} else if (coerceToType.equals(Float.class)) {
			return Float.valueOf(fromFieldValue);
		} else if (coerceToType.equals(Double.class)) {
			return Double.valueOf(fromFieldValue);
		} else if (coerceToType.equals(Boolean.class)) {
			return Boolean.valueOf(fromFieldValue);
		} else if (coerceToType.equals(String.class)) {
			return fromFieldValue;
		} else if (coerceToType.equals(Date.class)) {
			return stringToDateConversion(fromFieldValue, coerceToType, coerceToFormat);
		} else if (coerceToType.equals(BigDecimal.class)) {
			return new BigDecimal(fromFieldValue);
		} else {
			throw new UnsupportedCoerceException("Unsupported coerce. Cannot convert from java.lang.String to " + coerceToType.getName());
		}
	}

	public static Date stringToDateConversion(String fromFieldValue, Class coerceToType, String coerceToFormat)
			throws DateParseException, UnsupportedCoerceException {
		if (fromFieldValue == null) {
			return null;
		} else if (coerceToType.equals(Date.class)) {
			if (coerceToFormat == null || coerceToFormat.isEmpty()) {
				throw new DateParseException("Date format cannot be null or empty.");
			} else {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(coerceToFormat);
					return sdf.parse(fromFieldValue);
				} catch (ParseException parseException) {
					throw new DateParseException(parseException.getMessage(), parseException);
				}
			}
		} else {
			throw new UnsupportedCoerceException("Unsupported coerce. Cannot convert from java.lang.String to " + coerceToType.getName());
		}
	}

	public static Object dateToOffsetDateTypeConversion(Object fieldValue, Class fieldType, Class fieldToType, ZoneOffset zoneOffset)
			throws UnsupportedCoerceException {
		if (fieldValue == null) {
			return null;
		} else if (fieldValue.getClass().equals(Date.class) && fieldToType.equals(OffsetDateTime.class)) {
			Instant instant = ((Date) fieldValue).toInstant();
			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneOffset.of(zoneOffset.toString()));
			return offsetDateTime;
		} else {
			throw new UnsupportedCoerceException("Unsupported coerce. Cannot convert from " + fieldType.getName() + " to " + fieldToType.getName());
		}
	}
}
