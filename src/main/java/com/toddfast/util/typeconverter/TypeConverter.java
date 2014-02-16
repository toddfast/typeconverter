package com.toddfast.util.typeconverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an efficient and robust mechanism for converting an object
 * to a different type. For example, one can convert a {@link String} to
 * an {@link Integer} using the {@link TypeConverter} like this:
 *
 * <pre>
 *	Integer i = (Integer) TypeConverter.asType(Integer.class, "123");
 * </pre>
 *
 * or using the shortcut method:
 *
 * <pre>
 *	int i = TypeConverter.asInt("123");
 * </pre>
 *
 * The {@link TypeConverter} comes ready to convert all the primitive
 * types, plus a few more like {@link java.sql.Date} and
 * {@link java.math.BigDecimal}.<p>
 *
 * It is possible to register classes that implement the new
 * {@link TypeConversion} interface for conversion  to a custom type.
 * For example, this means that you can define a class to convert arbitrary
 * objects to type <code>Foo</code>, and register it for use throughout the VM:
 *
 * <pre>
 *	TypeConversion fooConversion = new FooTypeConversion();
 *	TypeConverter.registerTypeConversion(Foo.class, fooConversion);
 *	...
 *	Bar bar = new Bar();
 *	Foo foo = TypeConverter.asType(Foo.class, bar);
 *	...
 *	String s = "bar";
 *	Foo foo = TypeConverter.asType(Foo.class, s);
 * </pre>
 *
 * The TypeConverter allows specification of an arbitrary <i>type key</i> in 
 * the {@link #registerTypeConversion()} and {@link #asType() methods,
 * so one can simultaneously register a conversion object under a 
 * {@link Class} object, a class name, and a logical type name. For example,
 * the following are valid ways of converting a string to an <code>int</code>:
 *
 * <pre>
 *	Integer i = TypeConverter.asType(Integer.class, "123");
 *	Integer i = (Integer) TypeConverter.asType("java.lang.Integer", "123");
 *	Integer i = (Integer) TypeConverter.asType(TypeConverter.TYPE_INT, "123");
 *	Integer i = (Integer) TypeConverter.asType(TypeConverter.TYPE_INTEGER, "123");
 *	Integer i = (Integer) TypeConverter.asType("int", "123");
 *	Integer i = (Integer) TypeConverter.asType("integer", "123");
 *	int i = TypeConverter.asInt("123");
 * </pre>
 *
 * Default type conversions have been registered under the following keys:
 * 
 *	<pre>
 *	Classes:
 *		java.lang.Object
 *		java.lang.String
 *		java.lang.Integer
 *		java.lang.Integer.TYPE (int)
 *		java.lang.Double
 *		java.lang.Double.TYPE (double)
 *		java.lang.Boolean
 *		java.lang.Boolean.TYPE (boolean)
 *		java.lang.Long
 *		java.lang.Long.TYPE (long)
 *		java.lang.Float
 *		java.lang.Float.TYPE (float)
 *		java.lang.Short
 *		java.lang.Short.TYPE (short)
 *		java.lang.Byte
 *		java.lang.Byte.TYPE (byte)
 *		java.lang.Character
 *		java.lang.Character.TYPE (char)
 *		java.math.BigDecimal
 *		java.sql.Date
 *		java.sql.Time
 *		java.sql.Timestamp
 *
 *	Class name strings:
 *		"java.lang.Object"
 *		"java.lang.String"
 *		"java.lang.Integer"
 *		"java.lang.Double"
 *		"java.lang.Boolean"
 *		"java.lang.Long"
 *		"java.lang.Float"
 *		"java.lang.Short"
 *		"java.lang.Byte"
 *		"java.lang.Character"
 *		"java.math.BigDecimal"
 *		"java.sql.Date"
 *		"java.sql.Time"
 *		"java.sql.Timestamp"
 *
 *	Logical type name string constants:
 *		TypeConverter.TYPE_UNKNOWN ("null")
 *		TypeConverter.TYPE_OBJECT ("object")
 *		TypeConverter.TYPE_STRING ("string")
 *		TypeConverter.TYPE_INT ("int")
 *		TypeConverter.TYPE_INTEGER ("integer")
 *		TypeConverter.TYPE_DOUBLE ("double")
 *		TypeConverter.TYPE_BOOLEAN ("boolean")
 *		TypeConverter.TYPE_LONG ("long")
 *		TypeConverter.TYPE_FLOAT ("float")
 *		TypeConverter.TYPE_SHORT ("short")
 *		TypeConverter.TYPE_BYTE ("byte")
 *		TypeConverter.TYPE_CHAR ("char")
 *		TypeConverter.TYPE_CHARACTER ("character")
 *		TypeConverter.TYPE_BIG_DECIMAL ("bigdecimal")
 *		TypeConverter.TYPE_SQL_DATE ("sqldate")
 *		TypeConverter.TYPE_SQL_TIME ("sqltime")
 *		TypeConverter.TYPE_SQL_TIMESTAMP ("sqltimestamp")
 *	</pre>
 *
 * The {@link TypeConverter} treats type keys of type {@link Class}
 * slightly differently than other keys.  If the provided value is already of 
 * the type specified by the type key class, it is returned without a 
 * conversion taking place.  For example, a value of type <code>MySub</code> 
 * that extends the class <code>MySuper</code> would not be converted in the 
 * following situation because it is already of the necessary type:
 *
 * <pre>
 *	MySub o = TypeConverter.asType(MySuper.class, mySub);
 * </pre>
 *
 * Finally, a class can optionally implement the {@link Listener}
 * and/or {@link ConvertibleType} interfaces to receive conversion
 * events or provide its own {@link TypeConversion} instance, respectively.
 * This capability allows a class to implement very rich custom type
 * conversion logic.<p>
 *
 * Be warned that although the type conversion infrastructure in this class
 * is desgned to add only minimal overhead to the conversion process, conversion 
 * of an object to another type is a potentially expensive operation and should
 * be used with discretion.
 *
 * @see		ConvertibleType
 * @see		TypeConversion
 * @see		Listener
 */
public class TypeConverter {

	/**
	 * Cannot instantiate
	 *
	 */
	private TypeConverter() {
		super();
	}

	/**
	 * Return the map of type conversion objects.  The keys for the values
	 * in this map may be arbitrary objects, but the values are of type
	 * <code>TypeConversion</code>.
	 *
	 */
	public static Map<Object,TypeConversion> getTypeConversions() {
		return typeConversions;
	}

	/**
	 * Register a type conversion object under the specified key.  This
	 * method can be used by developers to register custom type conversion 
	 * objects.
	 *
	 */
	public static void registerTypeConversion(Object key, 
			TypeConversion conversion) {
		typeConversions.put(key,conversion);
	}

	/**
	 * Convert an object to the type specified by the provided type key.
	 * A type conversion object must have been previously registered
	 * under the provided key in order for the conversion to succeed (with
	 * one exception, see below).<p>
	 *
	 * Value objects that implement {@link Listener}
	 * interface will be notified of type conversion via the event methods
	 * declared in that interface. Value objects that implement {@link ConvertibleType}
	 * will be asked for an instance of {@link TypeConversion}
	 * directly, and the returned object will be used to convert the type 
	 * instead of the registered type conversion object.  Developers can use
	 * these interfaces to customize the type conversion process in robust
	 * and complex ways.<p>
	 *
	 * Note, this method treats type keys of type {@link Class}
	 * differently than other type keys.  That is, this method will check if
	 * the provided value is the same as or a subclass of the specified
	 * class. If it is, this method returns the value object immediately
	 * without attempting to convert its type.  One exception to this
	 * rule is if the provided type key is {@link Object.class}, in
	 * which case the conversion is attempted anyway.  The reason for this
	 * deviation is that this key may have special meaning based on the
	 * type of the provided value.  For example, if the provided value is
	 * a byte array, the {@link ObjectTypeConversion} class assumes
	 * it is a serialized object and attempts to deserialize it.  Because
	 * all objects, including arrays, are of type {@link Object},
	 * this conversion would never be attempted without this special
	 * handling. (Note that the default conversion for type key 
	 * {@link Object.class} is to simply return the original object.)
	 *
	 * @param	typeKey
	 *			The key under which the desired type conversion object
	 *			has been previously registered.  Most commonly, this key
	 *			should be a {@link Class} object, a class name string,
	 *			or a logical type string represented by the various 
	 *			<code>TYPE_*</code> constants defined in this class.
	 * @param	value
	 *			The value to convert to the specified target type
	 * @return	The converted value object, or <code>null</code> if the 
	 *			original value is <code>null</code>
	 */
	public static Object asType(Object typeKey, Object value) {

		if (value==null) {
			return null;
		}

		if (typeKey==null) {
			return value;
		}

		// Check if the provided value is already of the target type
		if (typeKey instanceof Class && ((Class)typeKey)!=Object.class
				&& ((Class)typeKey).isInstance(value)) {
			return value;
		}

		// Find the type conversion object
		TypeConversion conversion=null;
		if (value instanceof ConvertibleType) {
			conversion=((ConvertibleType)value).getTypeConversion(typeKey);
		}
		else {
			conversion=typeConversions.get(typeKey);
		}

		// Convert the value
		if (conversion!=null) {
			if (value instanceof Listener) {
				((Listener)value).beforeConversion(typeKey);
				return ((Listener)value).afterConversion(typeKey,
					conversion.convertValue(value));
			}
			else {
				return conversion.convertValue(value);
			}
		}
		else {
			throw new IllegalArgumentException("Could not find type "+
				"conversion for type \""+typeKey+"\" (value = \""+value+"\"");
		}
	}

	/**
	 * Return the value converted to the specified class type
	 *
	 */
	public static <C> C as(Class<C> type, Object value) {
		return (C)asType(type,value);
	}

	/**
	 * Return the value converted to a byte
	 *
	 */
	public static byte asByte(Object value) {
		return asByte(value,(byte)0);
	}

	/**
	 * Return the value converted to a byte or the default value
	 *
	 */
	public static byte asByte(Object value, byte defaultValue) {
		value=asType(Byte.class,value);
		if (value!=null) {
			return ((Byte)value).byteValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Return the value converted to a short
	 *
	 */
	public static short asShort(Object value) {
		return asShort(value,(short)0);
	}

	/**
	 * Return the value converted to a short or the default value
	 *
	 */
	public static short asShort(Object value, short defaultValue) {
		value=asType(Short.class,value);
		if (value!=null) {
			return ((Short)value).shortValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Return the value converted to an int
	 *
	 */
	public static int asInt(Object value) {
		return asInt(value,0);
	}

	/**
	 * Return the value converted to an int or the default value
	 *
	 */
	public static int asInt(Object value, int defaultValue) {
		value=asType(Integer.class,value);
		if (value!=null) {
			return ((Integer)value).intValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Return the value converted to a long
	 *
	 */
	public static long asLong(Object value) {
		return asLong(value,0L);
	}

	/**
	 * Return the value converted to a long or the default value
	 *
	 */
	public static long asLong(Object value, long defaultValue) {
		value=asType(Long.class,value);
		if (value!=null) {
			return ((Long)value).longValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Return the value converted to a float
	 *
	 */
	public static float asFloat(Object value) {
		return asFloat(value,0F);
	}

	/**
	 * Return the value converted to a float or the default value
	 *
	 */
	public static float asFloat(Object value, float defaultValue) {
		value=asType(Float.class,value);
		if (value!=null) {
			return ((Float)value).floatValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Return the value converted to a double
	 *
	 */
	public static double asDouble(Object value)
	{
		return asDouble(value,0D);
	}

	/**
	 * Return the value converted to a double or the default value
	 *
	 */
	public static double asDouble(Object value, double defaultValue) {
		value=asType(Double.class,value);
		return (value!=null)
			? ((Double)value).doubleValue()
			: defaultValue;
	}


	/**
	 * Return the value converted to a char
	 *
	 */
	public static char asChar(Object value) {
		return asChar(value,(char)0);
	}


	/**
	 * Return the value converted to a char or the default value
	 *
	 */
	public static char asChar(Object value, char defaultValue) {
		value=asType(Character.class,value);
		return (value!=null)
			? ((Character)value).charValue()
			: defaultValue;
	}

	/**
	 * Return the value converted to a boolean
	 *
	 */
	public static boolean asBoolean(Object value) {
		return asBoolean(value,false);
	}

	/**
	 * Return the value converted to a boolean or the default value
	 *
	 */
	public static boolean asBoolean(Object value, boolean defaultValue) {
		value=asType(Boolean.class,value);
		return (value!=null)
			? ((Boolean)value).booleanValue()
			: defaultValue;
	}

	/**
	 * Return the value converted to a string
	 *
	 */
	public static String asString(Object value) {
		return (String)asType(String.class,value);
	}

	/**
	 * Return the value converted to a string or the default value
	 *
	 */
	public static String asString(Object value, String defaultValue) {
		value=asType(String.class,value);
		return (value!=null)
			? (String)value
			: defaultValue;
	}




	////////////////////////////////////////////////////////////////////////////
	// Inner classes
	////////////////////////////////////////////////////////////////////////////

	/**
	 * During type conversion by the {@link TypeConverter} class, value
	 * objects that implement this interface will be called upon to provide 
	 * their own type conversion objects instead of using the conversions 
	 * registered with the {@link TypeConverter} class.
	 *
	 */
	public static interface ConvertibleType {

		/**
		 * Provides a custom type conversion object used to convert the type
		 * of this object.
		 * 
		 * @param	targetTypeKey
		 *			The target conversion key, normally a class or String.
		 * @return	A type conversion object valid for the specified conversion,
		 *			or null if the {@link TypeConverter} class should
		 *			attempt to use a previously registered type conversion 
		 *			object to convert the value of this object.
		 */
		public TypeConversion getTypeConversion(Object targetTypeKey);
	}

	/**
	 * The listener interface for receiving type conversion events.  A class
	 * that implements this interface will have the event methods in this
	 * interface called when that class is being converted by the various 
	 * conversion methods in the {@link TypeConverter} class.
	 *
	 */
	public static interface Listener {

		/**
		 * 
		 *
		 */
		public void beforeConversion(Object targetTypeKey);

		/**
		 * 
		 *
		 */
		public Object afterConversion(Object targetTypeKey, 
			Object convertedValue);
	}

	/**
	 * An object that can convert a value to a different type
	 *
	 */
	public static interface TypeConversion {

		/**
		 * Converts the provided value to the type represented by the
		 * implementer if this interface
		 *
		 */
		public Object convertValue(Object value);
	}
	
	/**
	 * Returns the value as-is (no conversion)
	 *
	 */
	public static class UnknownTypeConversion implements TypeConversion {

		@Override
		public Object convertValue(Object value) {
			return value;
		}
	}

	/**
	 * Converts the value to a string. If the value is a byte or char array,
	 * it is converted to a string via {@link toString()}.
	 *
	 */
	public static class StringTypeConversion implements TypeConversion {

		@Override
		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (value.getClass().isArray()) {
				// This is a byte array; we can convert it to a string
				if (value.getClass().getComponentType()==Byte.TYPE) {
					value=new String((byte[])value);
				}
				else
				if (value.getClass().getComponentType()==Character.TYPE) {
					value=new String((char[])value);
				}
			}
			else
			if (!(value instanceof String)) {
				value=value.toString();
			}

			return value;
		}
	}

	/**
	 * Convert to an integer by parsing the value as a string
	 *
	 */
	public static class IntegerTypeConversion implements TypeConversion {

		@Override
		public Object convertValue(Object value) {

			if (value==null) {
				return null;
			}

			if (!(value instanceof Integer)) {
				String v=value.toString();
				if (v.trim().length()==0) {
					value=null;
				}
				else {
					value=Integer.parseInt(v);
				}
			}

			return value;
		}
	}

	/**
	 * Convert to a double by parsing the value as a string
	 *
	 */
	public static class DoubleTypeConversion implements TypeConversion {

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

	/**
	 * Convert to a boolean by parsing the value as a string
	 *
	 */
	public static class BooleanTypeConversion implements TypeConversion {

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

	/**
	 * Convert to a long by parsing the value as a string
	 *
	 */
	public static class LongTypeConversion implements TypeConversion {

		@Override
		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (!(value instanceof Long)) {
				String v=value.toString();
				if (v.trim().length()==0) {
					value=null;
				}
				else {
					value=Long.parseLong(v);
				}
			}

			return value;
		}
	}

	/**
	 * Convert to a float by parsing the value as a string
	 *
	 */
	public static class FloatTypeConversion implements TypeConversion {

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

	/**
	 * Convert to a short by parsing the value as a string
	 *
	 */
	public static class ShortTypeConversion implements TypeConversion {

		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (!(value instanceof Short)) {
				String v=value.toString();
				if (v.trim().length()==0) {
					value=null;
				}
				else {
					value=Short.parseShort(v);
				}
			}

			return value;
		}
	}

	/**
	 * Convert to a {@link BigDecimal} by parsing the value as a string
	 *
	 */
	public static class BigDecimalTypeConversion implements TypeConversion {

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

	/**
	 * Convert to a byte by parsing the value as a string
	 *
	 */
	public static class ByteTypeConversion implements TypeConversion {

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

	/**
	 * Convert to a character by parsing the first character of the value
	 * as a string
	 *
	 */
	public static class CharacterTypeConversion implements TypeConversion {

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

	/**
	 * Convert to a {@link SqlDate} by parsing a value as a string of
	 * form <code>yyyy-[m]m-[d]d</code>.
	 *
	 * @see	java.sql.Date#valueOf(String)
	 */
	public static class SqlDateTypeConversion implements TypeConversion {

		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (!(value instanceof java.sql.Date)) {
				String v=value.toString();
				if (v.trim().length()==0) {
					value=null;
				}
				else {
					// Value must be in the "yyyy-mm-dd" format
					value=java.sql.Date.valueOf(v);
				}
			}

			return value;
		}
	}

	/**
	 * Convert to a {@link SqlTime} by parsing a value as a string of
	 * form <code>hh:mm:ss</code>.
	 *
	 * @see	java.sql.Date#valueOf(String)
	 */
	public static class SqlTimeTypeConversion implements TypeConversion {

		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (!(value instanceof java.sql.Time)) {
				String v=value.toString();
				if (v.trim().length()==0) {
					value=null;
				}
				else {
					// Value must be in the "hh:mm:ss" format
					value=java.sql.Time.valueOf(v);
				}
			}

			return value;
		}
	}

	/**
	 * Convert to a {@link SqlTimestamp} by parsing a value as a string of
	 * form <code>yyyy-[m]m-[d]d hh:mm:ss[.f...]</code>.
	 *
	 * @see	java.sql.Date#valueOf(String)
	 */
	public static class SqlTimestampTypeConversion implements TypeConversion {

		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (!(value instanceof java.sql.Timestamp)) {
				String v=value.toString();
				if (v.trim().length()==0) {
					value=null;
				}
				else {
					// Value must be in the "yyyy-mm-dd hh:mm:ss.fffffffff"
					// format
					value=java.sql.Timestamp.valueOf(v);
				}
			}

			return value;
		}
	}

	/**
	 * Converts a byte array to an object via deserialization, or returns the
	 * value as-is
	 *
	 */
	public static class ObjectTypeConversion implements TypeConversion {

		public Object convertValue(Object value) {
			if (value==null) {
				return null;
			}

			if (value.getClass().isArray()) {
				// This is a byte array; presume we can convert it to an object
				if (value.getClass().getComponentType()==Byte.TYPE) {
					ByteArrayInputStream bis=
						new ByteArrayInputStream((byte[])value);
					ObjectInputStream ois=null;
					try {
						ois=new ObjectInputStream(bis);
						value=ois.readObject();
					}
					catch (Exception e) {
						throw new IllegalArgumentException(
							"Could not deserialize object",e);
					}
					finally {
						try {
							if (ois!=null) {
								ois.close();
							}
						}
						catch (IOException e) {
							// Ignore
						}

						try {
							if (bis!=null) {
								bis.close();
							}
						}
						catch (IOException e) {
							// Ignore
						}
					}
				}
				else {
					; // value is OK as is
				}
			}

			return value;
		}
	}

	private static final Map<Object,TypeConversion> typeConversions=
		new HashMap<Object,TypeConversion>();

	/** Logical type name "null" */
	public static final String TYPE_UNKNOWN="null";

	/** Logical type name "object" */
	public static final String TYPE_OBJECT="object";

	/** Logical type name "string" */
	public static final String TYPE_STRING="string";

	/** Logical type name "int" */
	public static final String TYPE_INT="int";

	/** Logical type name "integer" */
	public static final String TYPE_INTEGER="integer";

	/** Logical type name "long" */
	public static final String TYPE_LONG="long";

	/** Logical type name "float" */
	public static final String TYPE_FLOAT="float";

	/** Logical type name "double" */
	public static final String TYPE_DOUBLE="double";

	/** Logical type name "short" */
	public static final String TYPE_SHORT="short";

	/** Logical type name "boolean" */
	public static final String TYPE_BOOLEAN="boolean";

	/** Logical type name "byte" */
	public static final String TYPE_BYTE="byte";

	/** Logical type name "char" */
	public static final String TYPE_CHAR="char";

	/** Logical type name "character" */
	public static final String TYPE_CHARACTER="character";

	/** Logical type name "bigdecimal" */
	public static final String TYPE_BIG_DECIMAL="bigdecimal";

	/** Logical type name "sqldate" */
	public static final String TYPE_SQL_DATE="sqldate";

	/** Logical type name "sqltime" */
	public static final String TYPE_SQL_TIME="sqltime";

	/** Logical type name "sqltimestamp" */
	public static final String TYPE_SQL_TIMESTAMP="sqltimestamp";

	public static final TypeConversion UNKNOWN_TYPE_CONVERSION=
		new UnknownTypeConversion();
	public static final TypeConversion OBJECT_TYPE_CONVERSION=
		new ObjectTypeConversion();
	public static final TypeConversion STRING_TYPE_CONVERSION=
		new StringTypeConversion();
	public static final TypeConversion INTEGER_TYPE_CONVERSION=
		new IntegerTypeConversion();
	public static final TypeConversion DOUBLE_TYPE_CONVERSION=
		new DoubleTypeConversion();
	public static final TypeConversion BOOLEAN_TYPE_CONVERSION=
		new BooleanTypeConversion();
	public static final TypeConversion LONG_TYPE_CONVERSION=
		new LongTypeConversion();
	public static final TypeConversion FLOAT_TYPE_CONVERSION=
		new FloatTypeConversion();
	public static final TypeConversion SHORT_TYPE_CONVERSION=
		new ShortTypeConversion();
	public static final TypeConversion BIG_DECIMAL_TYPE_CONVERSION=
		new BigDecimalTypeConversion();
	public static final TypeConversion BYTE_TYPE_CONVERSION=
		new ByteTypeConversion();
	public static final TypeConversion CHARACTER_TYPE_CONVERSION=
		new CharacterTypeConversion();
	public static final TypeConversion SQL_DATE_TYPE_CONVERSION=
		new SqlDateTypeConversion();
	public static final TypeConversion SQL_TIME_TYPE_CONVERSION=
		new SqlTimeTypeConversion();
	public static final TypeConversion SQL_TIMESTAMP_TYPE_CONVERSION=
		new SqlTimestampTypeConversion();

	static {
		// Add type conversions by class
		registerTypeConversion(Object.class,OBJECT_TYPE_CONVERSION);
		registerTypeConversion(String.class,STRING_TYPE_CONVERSION);
		registerTypeConversion(Integer.class,INTEGER_TYPE_CONVERSION);
		registerTypeConversion(Integer.TYPE,INTEGER_TYPE_CONVERSION);
		registerTypeConversion(Double.class,DOUBLE_TYPE_CONVERSION);
		registerTypeConversion(Double.TYPE,DOUBLE_TYPE_CONVERSION);
		registerTypeConversion(Boolean.class,BOOLEAN_TYPE_CONVERSION);
		registerTypeConversion(Boolean.TYPE,BOOLEAN_TYPE_CONVERSION);
		registerTypeConversion(Long.class,LONG_TYPE_CONVERSION);
		registerTypeConversion(Long.TYPE,LONG_TYPE_CONVERSION);
		registerTypeConversion(Float.class,FLOAT_TYPE_CONVERSION);
		registerTypeConversion(Float.TYPE,FLOAT_TYPE_CONVERSION);
		registerTypeConversion(Short.class,SHORT_TYPE_CONVERSION);
		registerTypeConversion(Short.TYPE,SHORT_TYPE_CONVERSION);
		registerTypeConversion(BigDecimal.class,BIG_DECIMAL_TYPE_CONVERSION);
		registerTypeConversion(Byte.class,BYTE_TYPE_CONVERSION);
		registerTypeConversion(Byte.TYPE,BYTE_TYPE_CONVERSION);
		registerTypeConversion(Character.class,CHARACTER_TYPE_CONVERSION);
		registerTypeConversion(Character.TYPE,CHARACTER_TYPE_CONVERSION);
		registerTypeConversion(java.sql.Date.class,SQL_DATE_TYPE_CONVERSION);
		registerTypeConversion(java.sql.Time.class,SQL_TIME_TYPE_CONVERSION);
		registerTypeConversion(java.sql.Timestamp.class,SQL_TIMESTAMP_TYPE_CONVERSION);

		// Add type conversions by class name
		registerTypeConversion(Object.class.getName(),OBJECT_TYPE_CONVERSION);
		registerTypeConversion(String.class.getName(),STRING_TYPE_CONVERSION);
		registerTypeConversion(Integer.class.getName(),INTEGER_TYPE_CONVERSION);
		registerTypeConversion(Double.class.getName(),DOUBLE_TYPE_CONVERSION);
		registerTypeConversion(Boolean.class.getName(),BOOLEAN_TYPE_CONVERSION);
		registerTypeConversion(Long.class.getName(),LONG_TYPE_CONVERSION);
		registerTypeConversion(Float.class.getName(),FLOAT_TYPE_CONVERSION);
		registerTypeConversion(Short.class.getName(),SHORT_TYPE_CONVERSION);
		registerTypeConversion(BigDecimal.class.getName(),BIG_DECIMAL_TYPE_CONVERSION);
		registerTypeConversion(Byte.class.getName(),BYTE_TYPE_CONVERSION);
		registerTypeConversion(Character.class.getName(),CHARACTER_TYPE_CONVERSION);
		registerTypeConversion(java.sql.Date.class.getName(),SQL_DATE_TYPE_CONVERSION);
		registerTypeConversion(java.sql.Time.class.getName(),SQL_TIME_TYPE_CONVERSION);
		registerTypeConversion(java.sql.Timestamp.class.getName(),SQL_TIMESTAMP_TYPE_CONVERSION);

		// Add type conversions by name
		registerTypeConversion(TYPE_UNKNOWN,UNKNOWN_TYPE_CONVERSION);
		registerTypeConversion(TYPE_OBJECT,OBJECT_TYPE_CONVERSION);
		registerTypeConversion(TYPE_STRING,STRING_TYPE_CONVERSION);
		registerTypeConversion(TYPE_INT,INTEGER_TYPE_CONVERSION);
		registerTypeConversion(TYPE_INTEGER,INTEGER_TYPE_CONVERSION);
		registerTypeConversion(TYPE_DOUBLE,DOUBLE_TYPE_CONVERSION);
		registerTypeConversion(TYPE_BOOLEAN,BOOLEAN_TYPE_CONVERSION);
		registerTypeConversion(TYPE_LONG,LONG_TYPE_CONVERSION);
		registerTypeConversion(TYPE_FLOAT,FLOAT_TYPE_CONVERSION);
		registerTypeConversion(TYPE_SHORT,SHORT_TYPE_CONVERSION);
		registerTypeConversion(TYPE_BIG_DECIMAL,BIG_DECIMAL_TYPE_CONVERSION);
		registerTypeConversion(TYPE_BYTE,BYTE_TYPE_CONVERSION);
		registerTypeConversion(TYPE_CHAR,CHARACTER_TYPE_CONVERSION);
		registerTypeConversion(TYPE_CHARACTER,CHARACTER_TYPE_CONVERSION);
		registerTypeConversion(TYPE_SQL_DATE,SQL_DATE_TYPE_CONVERSION);
		registerTypeConversion(TYPE_SQL_TIME,SQL_TIME_TYPE_CONVERSION);
		registerTypeConversion(TYPE_SQL_TIMESTAMP,SQL_TIMESTAMP_TYPE_CONVERSION);
	}
}
