/*
 * Copyright 2015 Imply Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.imply.wikiticker

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

sealed trait Writer
{
  def write(data: String): Unit

  def shutdown(): Unit
}

class ConsoleWriter extends Writer
{
  override def write(data: String): Unit = {
    System.out.println(data)
  }

  override def shutdown(): Unit = {}
}

class FileWriter(fileName: String) extends Writer
{
  private val outStream = new PrintStream(new FileOutputStream(new File(fileName)))

  override def write(data: String): Unit = {
    outStream.println(data)
  }

  override def shutdown(): Unit = {
    outStream.close()
  }
}

class KafkaWriter(brokers: String, topic: String) extends Writer
{
  private val props = new Properties()
  props.put("bootstrap.servers", brokers)
  props.put("acks", "all")
  props.put("retries", "3")

  private val producer = new KafkaProducer[String, String](props, new StringSerializer(), new StringSerializer())

  override def write(data: String): Unit = {
    producer.send(new ProducerRecord[String, String](topic, data)).get()
  }

  override def shutdown(): Unit = {
    producer.close()
  }
}
