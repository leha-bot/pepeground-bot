package com.pepeground.bot.repositories

import java.util.NoSuchElementException

import com.pepeground.bot.entities.ReplyEntity
import scalikejdbc._
import com.pepeground.bot.support.PostgreSQLSyntaxSupport._

object ReplyRepository {
  private val r = ReplyEntity.syntax("r")

  def getReplyOrCreateBy(pairId: Long, wordId: Option[Long])(implicit session: DBSession): ReplyEntity = {
    getReplyBy(pairId, wordId) match {
      case Some(reply: ReplyEntity) => reply
      case None => createReplyBy(pairId, wordId)
    }
  }

  def incrementReply(id: Long, counter: Long)(implicit session: DBSession): Unit = {
    withSQL {
      update(ReplyEntity).set(
        ReplyEntity.column.count -> (counter + 1)
      ).where.eq(ReplyEntity.column.id, id)
    }.update.apply()
  }

  def getReplyBy(pairId: Long, wordId: Option[Long])(implicit session: DBSession): Option[ReplyEntity] = {
    withSQL {
      select.from(ReplyEntity as r).where.eq(r.wordId, wordId).and.eq(r.pairId, pairId).limit(1)
    }.map(rs => ReplyEntity(r)(rs)).single.apply()
  }

  def createReplyBy(pairId: Long, wordId: Option[Long])(implicit session: DBSession): ReplyEntity = {
    withSQL {
      insert.into(ReplyEntity).namedValues(
        ReplyEntity.column.pairId -> pairId,
        ReplyEntity.column.wordId -> wordId
      ).onConflictDoNothing()
    }.update().apply()

    getReplyBy(pairId, wordId) match {
      case Some(reply: ReplyEntity) => reply
      case None => throw new NoSuchElementException("No such reply")
    }
  }
}