package implementation

import org.apache.spark.sql.catalyst.types.{ DataType, StructField, StructType }
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.apache.spark.sql.catalyst.types.ByteType
import org.apache.commons.lang.time.DurationFormatUtils
import org.apache.spark.sql.catalyst.types.StructField
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class AvroConverterTest extends FunSuite {
  val intField = new StructField("int", DataType("IntegerType"), false)
  val strField = new StructField("str", DataType("StringType"), true)
  val arrOfStrField = new StructField("arr", DataType("ArrayType(StringType,false)"), true)
  val arrOfStrFieldNotNullable = new StructField("arr", DataType("ArrayType(StringType,false)"), false)
  val arrOfRecField = new StructField("arrOfRec", DataType("ArrayType(StructType(List(StructField(str,StringType,true))),false)"), true)
  val mapOfStrField = new StructField("map", DataType("MapType(StringType,StringType,true)"), false)
  val mapOfRecField = new StructField("mapOfRec", DataType("MapType(StringType,StructType(List(StructField(str,StringType,true))),true)"), false)
  val structType = new StructType(Seq(intField, strField))
  val recordType = new StructField("record", structType, false)
  val byteField = new StructField("byte", DataType("ByteType"), false)
  val binaryField = new StructField("binary", DataType("BinaryType"), true)
  val arrOfArrString = new StructField("arrOfArrayString", DataType("ArrayType(ArrayType(StringType,false),false)"), true)
  val mapOfMap = new StructField("mapOfMap", DataType("MapType(StringType, MapType(StringType,StringType,true),true)"), false)
  val arrOfMapofArr = new StructField("arrOfMapOfArr", DataType("ArrayType(MapType(StringType, ArrayType(ArrayType(StringType,false),false),true),false)"), true)
  val decimalField = new StructField("decimal", DataType("DecimalType"), true)
  
  test("simple schema") {
    val result = AvroConverter.getAvroSchema(structType)
    assert(result.toString() == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"str\",\"type\":[\"string\",\"null\"]}]}")

  }

  test("schema with record") {
    val complexStructType = new StructType(Seq(strField, recordType))
    val result = AvroConverter.getAvroSchema(complexStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"str\",\"type\":[\"string\",\"null\"]}," +
      "{\"name\":\"record\",\"type\":{\"type\":\"record\",\"name\":\"record\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"str\",\"type\":[\"string\",\"null\"]}]}}]}")
  }

  test("schema with list of strings") {
    val arrStructType = new StructType(Seq(intField, arrOfStrField))
    val result = AvroConverter.getAvroSchema(arrStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"arr\",\"type\":[{\"type\":\"array\",\"items\":\"string\"},\"null\"]}]}")
  }

  test("schema with list of strings only") {
    val arrStructType = new StructType(Seq(arrOfStrField))
    val result = AvroConverter.getAvroSchema(arrStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[" +
      "{\"name\":\"arr\",\"type\":[{\"type\":\"array\",\"items\":\"string\"},\"null\"]}]}")
  }

  test("schema with list of strings not nullable") {
    val arrStructType = new StructType(Seq(intField, arrOfStrFieldNotNullable))
    val result = AvroConverter.getAvroSchema(arrStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"arr\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}")
  }

  test("schema with list of records") {
    val arrStructType = new StructType(Seq(intField, arrOfRecField))
    val result = AvroConverter.getAvroSchema(arrStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"arrOfRec\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\"," +
      "\"name\":\"arrOfRec\",\"fields\":[{\"name\":\"str\",\"type\":[\"string\",\"null\"]}]}},\"null\"]}]}")
  }

  test("schema with map of strings") {
    val mapStructType = new StructType(Seq(intField, mapOfStrField))
    val result = AvroConverter.getAvroSchema(mapStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"map\",\"type\":{\"type\":\"map\",\"values\":[\"string\",\"null\"]}}]}")
  }

  test("schema with map of records") {
    val mapStructType = new StructType(Seq(intField, mapOfRecField))
    val result = AvroConverter.getAvroSchema(mapStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"int\",\"type\":\"int\"}," +
      "{\"name\":\"mapOfRec\",\"type\":{\"type\":\"map\",\"values\":[{\"type\":\"record\"," +
      "\"name\":\"mapOfRec\",\"fields\":[{\"name\":\"str\",\"type\":[\"string\",\"null\"]}]},\"null\"]}}]}")
  }

  test("schema with byte type") {
    val result = AvroConverter.getAvroSchema(new StructType(Seq(byteField)))
    assert(result.toString() == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"byte\",\"type\":\"int\"}]}")
  }

  test("schema with list of list of strings") {
    val arrStructType = new StructType(Seq(arrOfArrString))
    val result = AvroConverter.getAvroSchema(arrStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"arrOfArrayString\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"array\",\"items\":\"string\"}},\"null\"]}]}")
  }

  test("schema with map of map") {
    val mapStructType = new StructType(Seq(mapOfMap))
    val result = AvroConverter.getAvroSchema(mapStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"mapOfMap\",\"type\":{\"type\":\"map\",\"values\":[{\"type\":\"map\",\"values\":[\"string\",\"null\"]},\"null\"]}}]}")
  }

  test("schema with list of Maps of lists") {
    val arrStructType = new StructType(Seq(arrOfMapofArr))
    val result = AvroConverter.getAvroSchema(arrStructType)
    assert(result.toString == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"arrOfMapOfArr\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"map\",\"values\":[{\"type\":\"array\",\"items\":{\"type\":\"array\",\"items\":\"string\"}},\"null\"]}},\"null\"]}]}")
  }

  test("schema with lot of fields") {
    var lots = Seq[StructField]()
    for (i <- 0 until 10000) {
      lots = lots ++ Seq(new StructField(s"str$i", DataType("StringType"), true))
    }
    val lotsStruct = new StructType(lots)
    val tryGetLogs = Try(AvroConverter.getAvroSchema(lotsStruct))
    assert(true, tryGetLogs.isSuccess)
  }
  
  
  test("schema with decimal type") {
    val result = AvroConverter.getAvroSchema(StructType(Seq(decimalField)))
    assert(result.toString() == "{\"type\":\"record\",\"name\":\"RECORD\",\"fields\":[{\"name\":\"decimal\",\"type\":[\"string\",\"null\"]}]}")

  }
}