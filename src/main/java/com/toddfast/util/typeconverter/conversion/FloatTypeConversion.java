package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a float by parsing the value as a string
 *
 */
public class FloatTypeConversion implements TypeConverter.TypeConversion {

	@Override
	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof Float)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=Float.parseFloat(v);
			}
		}
		return value;
	}
}
