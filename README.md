## wikiticker

<img src="https://cloud.githubusercontent.com/assets/1214075/9833538/f2dbbed4-594e-11e5-824e-6a309cbd3da9.jpg" />

This is a Scala library that reads the recent change feeds from
[Wikimedia's IRC channels](https://meta.wikimedia.org/wiki/IRC/Channels#Raw_feeds).

Usage:

```scala
import com.metamx.common.scala.concurrent.loggingThread
import io.imply.wikiticker.IrcTicker
import io.imply.wikiticker.Message
import io.imply.wikiticker.MessageListener

object Example
{
  def main(args: Array[String]) {
    val listener = new MessageListener {
      override def process(message: Message) = {
        println(s"User[${message.user}] edited page[${message.page}] at[${message.timestamp}].")
      }
    }
    val wikipedias = Seq("en", "sv", "de", "nl", "fr")
    val ticker = new IrcTicker(
      "irc.wikimedia.org",
      "ident",
      wikipedias map (x => s"#$x.wikipedia"),
      Seq(listener)
    )
    ticker.start()
    Runtime.getRuntime.addShutdownHook(
      loggingThread {
        ticker.stop()
      }
    )
    Thread.currentThread().join()
  }
}
```
