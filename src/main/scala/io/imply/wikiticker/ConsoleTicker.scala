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

import com.metamx.common.lifecycle.Lifecycle
import com.metamx.common.scala.Jackson
import com.metamx.common.scala.Logging
import com.metamx.common.scala.lifecycle._
import com.twitter.app.Flags

object ConsoleTicker extends Logging
{
  def main(args: Array[String]) {
    val flags = new Flags("wikiticker-console")
    val out = flags("out", "console", "output destination [console, file, kafka]")
    val fileName = flags("filename", "wikiticker.out", "file name for output")
    val brokers = flags("brokers", "localhost:9092", "Kafka [bootstrap.servers]")
    val topic = flags("topic", "wikipedia", "Kafka topic")

    flags.parseArgs(args)

    val writer = out() match {
      case "console" => new ConsoleWriter
      case "file" => new FileWriter(fileName())
      case "kafka" => new KafkaWriter(brokers(), topic())
      case _ => throw new IllegalArgumentException("-out must be one of [console, file, kafka]")
    }

    val listener = new MessageListener {
      override def process(message: Message) = {
        writer.write(Jackson.generate(message.toMap))
      }
    }

    val wikipedias = Seq(
      "en",
      "sv",
      "de",
      "nl",
      "fr",
      "war",
      "ru",
      "ceb",
      "it",
      "es",
      "vi",
      "pl",
      "ja",
      "pt",
      "zh",
      "uk",
      "ca",
      "fa",
      "sh",
      "no",
      "ar",
      "fi",
      "id",
      "ro",
      "hu",
      "cs",
      "ko",
      "sr",
      "ms",
      "tr",
      "min",
      "eo",
      "kk",
      "eu",
      "da",
      "bg",
      "sk",
      "hy",
      "he",
      "lt",
      "hr",
      "sl",
      "et",
      "uz",
      "gl",
      "nn",
      "la",
      "vo",
      "simple",
      "el",
      "ce",
      "hi",
      "be"
    )

    val ticker = new IrcTicker(
      "irc.wikimedia.org",
      "imply",
      wikipedias map (x => s"#$x.wikipedia"),
      Seq(listener)
    )

    val lifecycle = new Lifecycle

    lifecycle onStart {
      ticker.start()
    } onStop {
      ticker.stop()
      writer.shutdown()
    }

    try {
      lifecycle.start()
      lifecycle.join()
    }
    catch {
      case e: Throwable =>
        log.error(e, "Failed to start up, stopping and exiting.")
        lifecycle.stop()
    }
  }
}
