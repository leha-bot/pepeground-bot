package com.pepeground.core

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object CoreConfig extends CoreConfig

class CoreConfig {

  lazy val config: Config = ConfigFactory.load()

  object redis {
    private lazy val redisConfig = config.getConfig("redis")
    lazy val host: String = redisConfig.getString("host")
    lazy val port: Int = redisConfig.getInt("port")
  }

  object punctuation {
    private lazy val punctuationConfig = config.getConfig("punctuation")

    lazy val endSentence: List[String] = punctuationConfig.getStringList("endSentence").asScala.toList
  }

}