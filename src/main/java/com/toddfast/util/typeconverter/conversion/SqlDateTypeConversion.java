package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a {@link SqlDate} by parsing a value as a string of
 * form <code>yyyy-[m]m-[d]d</code>.
 *
 * @see	java.sql.Date#valueOf(String)
 */
public class SqlDateTypeConversion implements TypeConverter.TypeConversion {

	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof java.sql.Date)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				// Value must be in the "yyyy-mm-dd" format
				value=java.sql.Date.valueOf(v);
			}
		}
		return value;
	}
}
