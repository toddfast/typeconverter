package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

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
