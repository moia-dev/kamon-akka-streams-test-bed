import sbt._

object Dependencies {
  object Version {
    val akka         = "2.5.16"
    val akkaHttp     = "10.1.3"
    val aspectJ      = "1.9.1"
    val enumeratum   = "1.5.13"
    val kamon        = "1.1.3"
    val kamonAkka    = "1.1.2"
    val log4j        = "2.11.0"
    val scalaLogging = "3.9.0"
    val scalaCheck   = "1.14.0"
    val scalaTest    = "3.0.5"
  }
  val akkaKamon       = "io.kamon"                   %% "kamon-akka-2.5"    % Version.kamonAkka
  val logSlfOverLog4j = "org.apache.logging.log4j"   % "log4j-slf4j-impl"   % Version.log4j
  val akkaHttp        = "com.typesafe.akka"          %% "akka-http"         % Version.akkaHttp
  val akkaStream      = "com.typesafe.akka"          %% "akka-stream"       % Version.akka
  val akkaStreamsTestKit  = "com.typesafe.akka"      %% "akka-stream-testkit"        % Version.akka
  val akkaActorTestkit = "com.typesafe.akka"         %% "akka-testkit"      % Version.akka
  val enumeratum      = "com.beachape"               %% "enumeratum"        % Version.enumeratum
  val kamon           = "io.kamon"                   %% "kamon-core"        % Version.kamon
  val log4jApi        = "org.apache.logging.log4j"   % "log4j-api"          % Version.log4j
  val log4jCore       = "org.apache.logging.log4j"   % "log4j-core"         % Version.log4j
  val scalaLogging    = "com.typesafe.scala-logging" %% "scala-logging"     % Version.scalaLogging
  val scalaCheck      = "org.scalacheck"             %% "scalacheck"        % Version.scalaCheck
  val scalaTest       = "org.scalatest"              %% "scalatest"         % Version.scalaTest
}
