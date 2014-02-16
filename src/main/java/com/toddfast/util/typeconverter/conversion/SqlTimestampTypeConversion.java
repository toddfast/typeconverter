package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a {@link SqlTimestamp} by parsing a value as a string of
 * form <code>yyyy-[m]m-[d]d hh:mm:ss[.f...]</code>.
 *
 * @see	java.sql.Date#valueOf(String)
 */
public class SqlTimestampTypeConversion implements TypeConverter.TypeConversion {

	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof java.sql.Timestamp)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				// Value must be in the "yyyy-mm-dd hh:mm:ss.fffffffff"
				// format
				value=java.sql.Timestamp.valueOf(v);
			}
		}
		return value;
	}
}
