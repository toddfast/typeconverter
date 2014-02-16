package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a long by parsing the value as a string
 *
 */
public class LongTypeConversion implements TypeConverter.TypeConversion {

	@Override
	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof Long)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=Long.parseLong(v);
			}
		}
		return value;
	}
}
