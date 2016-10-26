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

import com.ircclouds.irc.api.Callback
import com.ircclouds.irc.api.IRCApiImpl
import com.ircclouds.irc.api.IServerParameters
import com.ircclouds.irc.api.domain.IRCServer
import com.ircclouds.irc.api.domain.messages.ChannelPrivMsg
import com.ircclouds.irc.api.listeners.VariousMessageListenerAdapter
import com.ircclouds.irc.api.state.IIRCState
import com.metamx.common.scala.Logging
import com.metamx.common.scala.Predef._
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.apache.commons.lang.StringEscapeUtils
import org.joda.time.DateTime
import scala.collection.JavaConverters._

class IrcTicker(
  server: String,
  nick: String,
  channels: Seq[String],
  listeners: Seq[MessageListener]
  ) extends Logging
{
  private val executorService = Executors.newSingleThreadScheduledExecutor()

  @volatile private var lastMessageTime = DateTime.now()
  @volatile private var irc             = ircCreate()

  def start(): Unit = {
    startWatchdog()
    ircConnect()
  }

  def stop(): Unit = {
    try {
      irc.disconnect()
      log.info("Disconnected from IRC server: %s", server)
    }
    catch {
      case e: Exception =>
        log.warn(e, "Failed to disconnect from IRC server: %s", server)
    }
  }

  def startWatchdog(): Unit = {
    executorService.scheduleAtFixedRate(
      new Runnable
      {
        override def run(): Unit = {
          if (lastMessageTime.plus(IrcTicker.WatchdogTimeoutMillis).isBeforeNow) {
            log.info("Watchdog timeout elapsed, restarting IRC client")
            lastMessageTime = DateTime.now()

            try {
              stop()
              irc = ircCreate()
              ircConnect()
            }
            catch {
              case e: Exception =>
                log.warn(e, "Failed to restart IRC client")
            }
          }
        }
      }, IrcTicker.WatchdogPeriodMillis, IrcTicker.WatchdogPeriodMillis, TimeUnit.MILLISECONDS
    )
  }

  def ircCreate(): IRCApiImpl = {
    new IRCApiImpl(false) withEffect { irc =>
      irc.addListener(
        new VariousMessageListenerAdapter
        {
          override def onChannelMessage(ircMessage: ChannelPrivMsg) = {
            if (log.isDebugEnabled) {
              log.debug(
                "Received IRC message from server[%s], channel[%s], user[%s]: %s",
                server,
                ircMessage.getChannelName,
                ircMessage.getSource.getNick,
                StringEscapeUtils.escapeJava(ircMessage.getText)
              )
            }

            lastMessageTime = DateTime.now()

            val maybeMessage: Option[Message] = try {
              Message.fromIrcMessage(DateTime.now, ircMessage.getChannelName, ircMessage.getText)
            }
            catch {
              case e: Exception =>
                log.warn(
                  e,
                  "Failed to decode ircMessage from source[%s] with text[%s].",
                  ircMessage.getSource,
                  ircMessage.getText
                )
                None
            }

            for (message <- maybeMessage; listener <- listeners) {
              try listener.process(message)
              catch {
                case e: Exception =>
                  log.warn(e, "Listener[%s] failed to process message: %s", listener, message)
              }
            }
          }
        }
      )
    }
  }

  def ircConnect(): Unit = {
    log.info("Connecting to IRC server: %s", server)
    irc.connect(
      new IServerParameters
      {
        override def getNickname = nick

        override def getServer = new IRCServer(server, false)

        override def getRealname = nick

        override def getAlternativeNicknames = Seq(s"$nick-${UUID.randomUUID()}").asJava

        override def getIdent = IrcTicker.Ident
      },
      new Callback[IIRCState]
      {
        override def onSuccess(ircState: IIRCState) = {
          log.info("Connected to IRC server: %s, nick is: %s", server, ircState.getNickname)
          for (channel <- channels) {
            log.info("Joining channel: %s", channel)
            irc.joinChannel(channel)
          }
        }

        override def onFailure(e: Exception) = {
          log.error(e, "Unable to connect to IRC server: %s", server)
        }
      }
    )
  }
}

object IrcTicker
{
  val Ident                 = "wikiticker"
  val WatchdogPeriodMillis  = 15000L
  val WatchdogTimeoutMillis = 60000L
}
