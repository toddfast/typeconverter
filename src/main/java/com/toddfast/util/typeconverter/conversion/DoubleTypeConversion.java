package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a double by parsing the value as a string
 *
 */
public class DoubleTypeConversion implements TypeConverter.TypeConversion {

	@Override
	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof Double)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=Double.parseDouble(v);
			}
		}
		return value;
	}
}
