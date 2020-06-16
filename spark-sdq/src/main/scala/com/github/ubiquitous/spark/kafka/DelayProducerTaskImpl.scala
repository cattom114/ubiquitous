package com.github.ubiquitous.spark.kafka

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.kafka.clients.producer.{Producer, ProducerRecord, RecordMetadata}
import org.apache.log4j.Logger

/**
  *
  * @author Namhwik on 2020-05-13 18:00
  */


class DelayProducerTaskImpl(dl: Int, k: String, msg: String)(implicit producer: Producer[String, String]) extends
  DelayTask[String, String](dl: Int, k: String, msg: String) {
  val logger: Logger = Logger.getLogger(this.getClass)
  val startDate = new Date()

  override def call(): Unit = {
    def send() = {
      producer.send(
        new ProducerRecord[String, String](k, msg),
        new KafkaCacheKeyCallback[String, Unit](k, removeKey)
      )
    }

    var attempt = 0
    var recordMetaData: RecordMetadata = null
    var future = send()

    while (attempt < 3 || recordMetaData != null) {
      try {
        recordMetaData = future.get()
      } catch {
        case ex: Exception =>
          logger.error(s"${ex.getMessage},error occurred when running delay producer task $k, $msg")
          attempt += 1
          future = send()
      }
    }


    println(s" after ${(new Date().getTime - startDate.getTime) / 1000} seconds , ** $dl finished ** ${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.Sss").format(new Date())}")
  }
}