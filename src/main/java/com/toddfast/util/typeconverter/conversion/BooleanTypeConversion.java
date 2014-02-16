package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a boolean by parsing the value as a string
 *
 */
public class BooleanTypeConversion implements TypeConverter.TypeConversion {

	@Override
	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof Boolean)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=Boolean.parseBoolean(v);
			}
		}
		return value;
	}
}
