package com.toddfast.util.typeconverter;

import com.toddfast.util.typeconverter.TypeConverter.ConvertibleType;
import com.toddfast.util.typeconverter.TypeConverter.Listener;
import com.toddfast.util.typeconverter.TypeConverter.Conversion;

/**
 *
 *
 */
public class TestSubclass extends TestSuperclass
		implements Listener, ConvertibleType {

	public void beforeConversion(Object targetTypeKey) {
		System.out.println("--- beforeConversion("+targetTypeKey+")");
	}

	public Object afterConversion(Object targetTypeKey, Object convertedValue) {
		System.out.println("--- afterConversion("+
			targetTypeKey+","+convertedValue+")");
		return convertedValue;
	}

	public Conversion getTypeConversion(final Object targetTypeKey) {
		return new Conversion() {

			@Override
			public Object[] getTypeKeys() {
				return new Object[] { TestSubclass.class };
			}

			@Override
			public Object convert(Object value) {
				System.out.println("--- Converting value to type \""+key+
					"\": "+value);
				return "Converted Test value";
			}

			private Object key=targetTypeKey;
		};
	}
}
