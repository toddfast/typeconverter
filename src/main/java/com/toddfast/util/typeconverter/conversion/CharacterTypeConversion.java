package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;

/**
 * Convert to a character by parsing the first character of the value
 * as a string
 *
 */
public class CharacterTypeConversion implements TypeConverter.TypeConversion {

	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof Character)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=new Character(v.charAt(0));
			}
		}
		return value;
	}
}
