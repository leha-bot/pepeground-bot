lazy val versions = new {
  val akka = "2.6.9"
  val scalikejdbc = "3.5.0"
  val telegram4s = "2.9.5"
  val twitter4s = "6.2"
  val akkaQuartzScheduler = "1.8.4-akka-2.6.x"
  val flyway = "6.5.3"
  val scalaLogging = "3.9.2"
  val logback = "1.2.3"
  val typesafeConfig = "1.4.0"
  val postgresql = "42.2.14"
  val commonsDbcp2 = "2.7.0"
  val jodaTime = "2.10.6"
  val redisClient = "3.30"
  val sentryLogback = "1.7.30"
  val scalatest = "3.0.1"
}

lazy val commonSettings = Seq(
  organization := "com.pepeground",
  scalaVersion := "2.12.6",
  version := "0.1",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Flyway" at "https://flywaydb.org/repo"
  ),
  parallelExecution in Test := false,
  test in assembly := {},
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % versions.logback,
    "com.typesafe" % "config" % versions.typesafeConfig,
    "org.scalikejdbc" %% "scalikejdbc"  % versions.scalikejdbc,
    "org.scalikejdbc" %% "scalikejdbc-joda-time"  % versions.scalikejdbc,
    "org.scalikejdbc" %% "scalikejdbc-config" % versions.scalikejdbc,
    "org.postgresql" % "postgresql" % versions.postgresql,
    "org.apache.commons" % "commons-dbcp2" % versions.commonsDbcp2,
    "joda-time" % "joda-time" % versions.jodaTime,
    "net.debasishg" %% "redisclient" % versions.redisClient,
    "com.typesafe.akka" %% "akka-actor" % versions.akka,
    "com.typesafe.scala-logging" %% "scala-logging" % versions.scalaLogging,
    "org.flywaydb" % "flyway-core" % versions.flyway,
    "io.sentry" % "sentry-logback" % versions.sentryLogback,
    "org.scalatest" %% "scalatest" % versions.scalatest % "test",
    "org.scalikejdbc" %% "scalikejdbc-test" % versions.scalikejdbc % "test"
  )
)

lazy val core = (project in file("core")).
  settings(
    commonSettings,
    name := "core"
  )

lazy val bot = (project in file("bot")).
  dependsOn(core).
  settings(
    commonSettings,
    name := "bot",
    mainClass in (Compile,run) := Some("com.pepeground.bot.Main"),
    libraryDependencies ++= Seq(
      "com.enragedginger" %% "akka-quartz-scheduler" % versions.akkaQuartzScheduler,
      "com.danielasfregola" %% "twitter4s" % versions.twitter4s,
      "info.mukel" % "telegrambot4s_2.12" % versions.telegram4s
    )
  )

lazy val api = (project in file("api")).
  dependsOn(core).
  settings(
    commonSettings,
    name := "api",
    mainClass in (Compile,run) := Some("com.pepeground.api.Main"),
    libraryDependencies ++= Seq(
      "org.jruby" % "jruby" % "9.1.+"
    )
  )

lazy val root = (project in file("."))
  .aggregate(bot, api, core)
