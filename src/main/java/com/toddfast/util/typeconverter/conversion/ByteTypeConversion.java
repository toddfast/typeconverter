package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a byte by parsing the value as a string
 *
 */
public class ByteTypeConversion implements TypeConverter.TypeConversion {

	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof Byte)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=Byte.parseByte(v);
			}
		}
		return value;
	}
}
