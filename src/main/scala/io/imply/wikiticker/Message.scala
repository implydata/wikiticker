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

import com.metamx.common.scala.untyped.Dict
import io.imply.wikiticker.Message._
import java.util.regex.Pattern
import org.joda.time.DateTime

case class Message(
  timestamp: DateTime,
  channel: String,
  page: String,
  flags: String,
  diffUrl: String,
  user: String,
  delta: Long,
  comment: String
)
{
  def isRobot = flags contains "B"

  def isMinor = flags contains "M"

  def isNew = flags contains "N"

  def isUnpatrolled = flags contains "!"

  def namespace = page match {
    case NamespaceRegex(ns) => ns
    case _ => "Main"
  }

  def added = math.max(delta, 0)

  def deleted = -1 * math.min(delta, 0)

  def toMap = Dict(
    "timestamp" -> timestamp.toString(),
    "channel" -> channel,
    "page" -> page,
    "flags" -> flags,
    "diffUrl" -> diffUrl,
    "user" -> user,
    "delta" -> delta,
    "added" -> added,
    "deleted" -> deleted,
    "comment" -> comment,
    "isRobot" -> isRobot,
    "isMinor" -> isMinor,
    "isNew" -> isNew,
    "isUnpatrolled" -> isUnpatrolled,
    "namespace" -> namespace
  )
}

object Message
{
  val NamespaceRegex = """^([^:]+):\S.*""".r

  val MessagePattern = Pattern.compile(
    """\x0314\[\[\x0307(.+?)\x0314\]\]\x034 (.*?)\x0310.*\x0302(http.+?)""" +
      """\x03.+\x0303(.+?)\x03.+\x03 (\(([+-]\d+)\).*|.+) \x0310(.+)\x03"""
  )

  def fromIrcMessage(timestamp: DateTime, channel: String, string: String): Option[Message] = {
    val m = MessagePattern.matcher(string)
    if (m.matches()) {
      val page = m.group(1)
      val flags = m.group(2).replaceAll( """\s+""", "")
      val diffUrl = m.group(3)
      val user = m.group(4)
      val delta = Option(m.group(6)).map(_.toLong).getOrElse(0L)
      val comment = m.group(7)
      Some(Message(timestamp, channel, page, flags, diffUrl, user, delta, comment))
    } else {
      None
    }
  }
}
