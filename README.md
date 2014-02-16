Easy Java Type Conversion
=========================

This lightweight library (*with no dependencies*) provides a versatile and robust mechanism for converting a Java object to a different type. For example, you can convert a `String` to an `Integer` using the TypeConverter like this:

```java
Integer i = TypeConverter.convert(Integer.class, "123");
```

or using the shortcut method:

```java
int i = TypeConverter.asInt("123");
```

Usage
-----

The primary class, `TypeConverter`, comes ready to convert all the primitive Java types, plus a few more like `java.sql.Date` and `BigDecimal`.

`TypeConverter` allows specification of an arbitrary type key in the `registerTypeConversion(Object,TypeConversion)` and `convert(Object,Object)` methods. This allows a conversion object to simultaneously be registered under various keys, such as a `Class` instance, a class name, and one or more logical type names. For example, the following are valid ways of converting a string to an int:

```java
Integer i = TypeConverter.convert(Integer.class, "123");
Integer i = (Integer) TypeConverter.convert("java.lang.Integer", "123");
Integer i = (Integer) TypeConverter.convert(TypeConverter.TYPE_INT, "123");
Integer i = (Integer) TypeConverter.convert(TypeConverter.TYPE_INTEGER, "123");
Integer i = (Integer) TypeConverter.convert("int", "123");
Integer i = (Integer) TypeConverter.convert("integer", "123");
int i = TypeConverter.asInt("123");
```

Default type conversions have been registered under the following keys:

```
Classes:
        java.lang.Object
        java.lang.String
        java.lang.Integer
        java.lang.Integer.TYPE (int)
        java.lang.Double
        java.lang.Double.TYPE (double)
        java.lang.Boolean
        java.lang.Boolean.TYPE (boolean)
        java.lang.Long
        java.lang.Long.TYPE (long)
        java.lang.Float
        java.lang.Float.TYPE (float)
        java.lang.Short
        java.lang.Short.TYPE (short)
        java.lang.Byte
        java.lang.Byte.TYPE (byte)
        java.lang.Character
        java.lang.Character.TYPE (char)
        java.math.BigDecimal
        java.sql.Date
        java.sql.Time
        java.sql.Timestamp

Class name strings:
        "java.lang.Object"
        "java.lang.String"
        "java.lang.Integer"
        "java.lang.Double"
        "java.lang.Boolean"
        "java.lang.Long"
        "java.lang.Float"
        "java.lang.Short"
        "java.lang.Byte"
        "java.lang.Character"
        "java.math.BigDecimal"
        "java.sql.Date"
        "java.sql.Time"
        "java.sql.Timestamp"

Logical type name string constants:
        TypeConverter.TYPE_UNKNOWN ("null")
        TypeConverter.TYPE_OBJECT ("object")
        TypeConverter.TYPE_STRING ("string")
        TypeConverter.TYPE_INT ("int")
        TypeConverter.TYPE_INTEGER ("integer")
        TypeConverter.TYPE_DOUBLE ("double")
        TypeConverter.TYPE_BOOLEAN ("boolean")
        TypeConverter.TYPE_LONG ("long")
        TypeConverter.TYPE_FLOAT ("float")
        TypeConverter.TYPE_SHORT ("short")
        TypeConverter.TYPE_BYTE ("byte")
        TypeConverter.TYPE_CHAR ("char")
        TypeConverter.TYPE_CHARACTER ("character")
        TypeConverter.TYPE_BIG_DECIMAL ("bigdecimal")
        TypeConverter.TYPE_SQL_DATE ("sqldate")
        TypeConverter.TYPE_SQL_TIME ("sqltime")
        TypeConverter.TYPE_SQL_TIMESTAMP ("sqltimestamp")
```

The `TypeConverter` treats type keys of type `Class` slightly differently than other keys. If the provided value is already of the type specified by the type key class, it is returned without a conversion taking place. For example, a value of type MySub that extends the class `MySuper` would not be converted in the following situation because it is already of the necessary type:

```java
MySub o = TypeConverter.convert(MySuper.class, mySub);
```

Extensibility
-------------

`TypeConverter` is extensible by registering classes that implement the `TypeConverter.Conversion` interface for conversion to a custom type. For example, you can define a class to convert arbitrary objects to type `Foo`, and register it for use throughout your application:

```java
// Register your custom conversion class
TypeConverter.registerTypeConversion(new FooConversion());

Foo foo;

Bar bar = new Bar();
foo = TypeConverter.convert(Foo.class, bar);

String s = "bar";
foo = TypeConverter.convert(Foo.class, s);
```

### Discovery

`TypeConverter.Conversion` classes are also discovered using the JDK's standard `java.util.ServiceLoader` mechanism. To make a conversion discoverable, place a file named

```
META-INF/services/com.toddfast.util.typeconverter.TypeConverter$Conversion
```

in your project, the contents of which are fully qualified `TypeConverter.Conversion` class names, one per line. See the `ServiceLoader` [documentation](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) for more details on how to use the `META-INF/services` mechanism, or (take a look at how registration of the default conversions is done](https://github.com/toddfast/typeconverter/blob/master/src/main/resources/META-INF/services/com.toddfast.util.convert.TypeConverter%24Conversion).


### Customizing conversion

Finally, a class can optionally implement the `TypeConverter.Listener` and/or `TypeConverter.Convertible` interfaces to receive conversion events or provide its own `TypeConverter.Conversion` instance, respectively. This capability allows a class to implement very rich custom type conversion logic.


Getting started
---------------

This library is in Maven Central and can be used with the following dependency:

```xml
<dependency>
  <groupId>com.toddfast.typeconverter</groupId>
  <artifactId>typeconverter</artifactId>
  <version>1.0</version>
</dependency>
```

Or, you can [download the artifacts directly](http://search.maven.org/#search%7Cga%7C1%7Ccom.toddfast.typeconverter).
