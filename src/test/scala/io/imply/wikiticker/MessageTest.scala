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

import org.joda.time.DateTime
import org.scalatest.FunSuite
import org.scalatest.ShouldMatchers

class MessageTest extends FunSuite with ShouldMatchers
{
  test("fromIrcMessage: Main namespace, all flags, added") {
    val message = Message.fromIrcMessage(
      new DateTime("2000"),
      "foo",
      "\u000314[[\u000307Bulgurluk: Ere\u011Fli\u000314]]\u00034 !MBN\u000310 \u000302" +
        "https://vi.wikipedia.org/w/index.php?diff=21612302&oldid=21227851&rcid=40213078" +
        "\u0003 \u00035*\u0003 \u000303TuanminhBot\u0003 \u00035*\u0003 (+18) \u000310AlphamaEditor\u0003"
    ).get

    message should be(
      Message(
        new DateTime("2000"),
        "foo",
        "Bulgurluk: Ereğli",
        "!MBN",
        "https://vi.wikipedia.org/w/index.php?diff=21612302&oldid=21227851&rcid=40213078",
        "TuanminhBot",
        18L,
        "AlphamaEditor"
      )
    )

    message.namespace should be("Main")
    message.isRobot should be(true)
    message.isUnpatrolled should be(true)
    message.isMinor should be(true)
    message.isNew should be(true)
    message.added should be(18L)
    message.deleted should be(0L)
  }

  test("fromIrcMessage: User talk namespace, no flags, deleted") {
    val message = Message.fromIrcMessage(
      new DateTime("2000"),
      "foo",
      "\u000314[[\u000307User talk:Bulgurluk: Ere\u011Fli\u000314]]\u00034 \u000310 \u000302" +
        "https://vi.wikipedia.org/w/index.php?diff=21612302&oldid=21227851&rcid=40213078" +
        "\u0003 \u00035*\u0003 \u000303TuanminhBot\u0003 \u00035*\u0003 (-18) \u000310AlphamaEditor\u0003"
    ).get

    message should be(
      Message(
        new DateTime("2000"),
        "foo",
        "User talk:Bulgurluk: Ereğli",
        "",
        "https://vi.wikipedia.org/w/index.php?diff=21612302&oldid=21227851&rcid=40213078",
        "TuanminhBot",
        -18L,
        "AlphamaEditor"
      )
    )

    message.namespace should be("User talk")
    message.isRobot should be(false)
    message.isUnpatrolled should be(false)
    message.isMinor should be(false)
    message.isNew should be(false)
    message.added should be(0L)
    message.deleted should be(18L)
  }
}
