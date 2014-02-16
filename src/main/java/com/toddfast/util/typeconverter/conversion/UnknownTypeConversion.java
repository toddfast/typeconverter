package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Returns the value as-is (no conversion)
 *
 */
public class UnknownTypeConversion implements TypeConverter.TypeConversion {

	@Override
	public Object convertValue(Object value) {
		return value;
	}
}
