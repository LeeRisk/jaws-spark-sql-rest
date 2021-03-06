package apiactors

import akka.actor.Actor
import apiactors.ActorOperations._
import implementation.SchemaSettingsFactory.{Hdfs, Hive, Parquet, Tachyon}
import implementation.{AvroConverter, HiveContextWrapper}
import messages.GetDatasourceSchemaMessage
import org.apache.spark.scheduler.HiveUtils
import org.apache.spark.sql.catalyst.types.StructType
import org.apache.spark.sql.parquet.ParquetUtils._
import server.Configuration

import scala.util.Try

/**
 * Created by lucianm on 06.02.2015.
 */
class GetDatasourceSchemaActor(hiveContext: HiveContextWrapper) extends Actor {

  def receive = {
    case request: GetDatasourceSchemaMessage =>

      val hostname: String = Configuration.rddDestinationIp.get
      val path: String = s"${request.path}"
      var message: String = s"Getting the data source schema for path $path, sourceType ${request.sourceType}, storageType ${request.storageType}"
      Configuration.log4j.info(message)

      val response = Try {
        var result: StructType = null
        request.sourceType match {
          case Hive() =>
            result = hiveContext.table(path).schema
          case Parquet() =>
            request.storageType match {
              case Hdfs() =>
                val hdfsURL = HiveUtils.getHdfsPath(hostname)
                result = hiveContext.readXPatternsParquet(hdfsURL, path).schema
              case Tachyon() =>
                val tachyonURL = HiveUtils.getTachyonPath(hostname)
                result = hiveContext.readXPatternsParquet(tachyonURL, path).schema
            }
        }

        val avroSchema = AvroConverter.getAvroSchema(result).toString(true)
        Configuration.log4j.debug(avroSchema)
        message = avroSchema
      }
      returnResult(response, message, "GET data source schema failed with the following message: ", sender)

    case request: Any => Configuration.log4j.error(request.toString)
  }

}
