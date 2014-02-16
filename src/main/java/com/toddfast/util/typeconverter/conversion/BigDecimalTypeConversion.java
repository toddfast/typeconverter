package com.toddfast.util.typeconverter.conversion;

import com.toddfast.util.typeconverter.TypeConverter;
import java.math.BigDecimal;

/**
 * Convert to a {@link BigDecimal} by parsing the value as a string
 *
 */
public class BigDecimalTypeConversion implements TypeConverter.TypeConversion {

	public Object convertValue(Object value) {
		if (value==null) {
			return null;
		}
		if (!(value instanceof BigDecimal)) {
			String v=value.toString();
			if (v.trim().length()==0) {
				value=null;
			}
			else {
				value=new BigDecimal(v);
			}
		}
		return value;
	}
}
