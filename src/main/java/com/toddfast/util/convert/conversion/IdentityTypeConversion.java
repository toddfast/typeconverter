package com.toddfast.util.convert.conversion;

import com.toddfast.util.convert.TypeConverter;

/**
 * Returns the value as-is (no conversion)
 *
 */
public class IdentityTypeConversion 
		implements TypeConverter.Conversion<Object> {

	@Override
	public Object[] getTypeKeys() {
		return new Object[] {};
	}

	@Override
	public Object convert(Object value) {
		return value;
	}
}
