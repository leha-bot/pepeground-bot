package com.pepeground.bot

import com.pepeground.bot.handlers._
import com.typesafe.scalalogging._
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._
import org.slf4j.LoggerFactory
import scalikejdbc._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Router extends TelegramBot with Polling with Commands {
  def token: String = Config.bot.telegramToken

  override val logger: Logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val botName = Config.bot.name.toLowerCase

  override def onMessage(msg: Message): Unit = DB localTx { implicit session =>
    Try(processMessage(msg)) match {
      case Success(_: Unit) =>
      case Failure(e: Throwable) => throw e
    }
  }

  private def processMessage(msg: Message)(implicit session: DBSession): Unit = for (text <- msg.text) cleanCmd(text) match {
    case c if expectedCmd(c, "/repost") => repostHandler(text, msg)
    case c if expectedCmd(c, "/get_stats") => getStatsHandler(text, msg)
    case c if expectedCmd(c, "/cool_story") => coolStoryHandler(text, msg)
    case c if expectedCmd(c, "/set_gab") => setGabHandler(text, msg)
    case c if expectedCmd(c, "/get_gab") => getGabHandler(text, msg)
    case c if expectedCmd(c, "/ping") => pingHandler(text, msg)
    case c if expectedCmd(c, "/set_repost_channel") => setRepostChannel(text, msg)
    case c if expectedCmd(c, "/get_repost_channel") => getRepostChat(text, msg)
    case s => if (!s.startsWith("/")) handleMessage(msg)
  }

  private def cleanCmd(cmd: String): String = cmd.takeWhile(_ != ' ').toLowerCase

  private def expectedCmd(cmd: String, expected: String): Boolean = cmd.split("@") match {
    case Array(c: String, name: String) => c == expected && name.toLowerCase == botName
    case Array(c: String) => c == expected
    case _ => false
  }

  private def handleMessage(msg: Message)(implicit session: DBSession): Unit = MessageHandler(msg).call() match {
    case Some(res: Either[Option[String], Option[String]]) => res match {
      case Left(s: Option[String]) => if (s.nonEmpty) makeResponse("message", SendMessage(msg.source, s.get, replyToMessageId = msg.messageId))
      case Right(s: Option[String]) => if (s.nonEmpty) makeResponse("message", SendMessage(msg.source, s.get))
    }
    case _ =>
  }

  private def makeResponse(context: String, msg: SendMessage): Unit = {
    logger.info("Response for %s, with text: %s".format(context, msg.text))
    request(msg)
  }

  private def setGabHandler(text: String, msg: Message)(implicit session: DBSession): Unit = {
    val level: Option[Int] = text.split(" ").take(2) match {
      case Array(_, randomLevel) => Try(randomLevel.toInt).toOption match {
        case Some(l: Int) => Some(l)
        case None => None
      }
      case _ => None
    }

    level match {
      case Some(l: Int) =>
        SetGabHandler(msg).call(l) match {
          case Some(s: String) => makeResponse(text, SendMessage(msg.source, s, replyToMessageId = msg.messageId))
          case None =>
        }
      case None => makeResponse(text, SendMessage(msg.source, "Wrong percent", replyToMessageId = msg.messageId))
    }
  }

  private def getGabHandler(text: String, msg: Message)(implicit session: DBSession): Unit = GetGabHandler(msg).call() match {
    case Some(s: String) => makeResponse(text, SendMessage(msg.source, s, replyToMessageId = msg.messageId))
    case None =>
  }

  private def getStatsHandler(text: String, msg: Message)(implicit session: DBSession): Unit = GetStatsHandler(msg).call() match {
    case Some(s: String) => makeResponse(text, SendMessage(msg.source, s, replyToMessageId = msg.messageId))
    case None =>
  }

  private def pingHandler(text: String, msg: Message)(implicit session: DBSession): Unit = PingHandler(msg).call() match {
    case Some(s: String) => makeResponse(text, SendMessage(msg.source, s, replyToMessageId = msg.messageId))
    case None =>
  }

  private def coolStoryHandler(text: String, msg: Message)(implicit session: DBSession): Unit = CoolStoryHandler(msg).call() match {
    case Some(s: String) => makeResponse(text, SendMessage(msg.source, s))
    case None =>
  }

  private def setRepostChannel(text: String, msg: Message)(implicit session: DBSession): Unit = {
    val chatUsername: Option[String] = msg.entities match {
      case Some(msgEntities: Seq[MessageEntity]) =>
        msgEntities.find(_.`type` == "mention") match {
          case Some(msgEntity: MessageEntity) =>
            val offset: Int = msgEntity.offset
            text.substring(offset, offset + msgEntity.length)
          case None => None
        }
      case _ => None
    }

    val chatMemberRequest: Option[Future[ChatMember]] = msg.from.flatMap(u => request(GetChatMember(Left(msg.chat.id), u.id.toLong)))

    chatUsername match {
      case Some(l: String) =>
        SetRepostChatHandler(msg, chatMemberRequest).call(l) match {
          case Some(s: String) => makeResponse(text, SendMessage(msg.source, s, replyToMessageId = msg.messageId))
          case None =>
        }
      case None => makeResponse(text, SendMessage(msg.source, "No chat username", replyToMessageId = msg.messageId))
    }
  }

  private def getRepostChat(text: String, msg: Message)(implicit session: DBSession): Unit = GetRepostChatHandler(msg).call() match {
    case Some(s: String) => makeResponse(text, SendMessage(msg.source, s, replyToMessageId = msg.messageId))
    case None =>
  }

  private def repostHandler(text: String, msg: Message)(implicit session: DBSession): Unit= RepostHandler(msg).call() match {
    case Some(s: ForwardMessage) =>
      request(s) onComplete {
        case Success(_) => makeResponse(text, SendMessage(msg.source, "reposted", replyToMessageId = msg.messageId))
        case Failure(_) =>
      }

    case None =>
  }
}
