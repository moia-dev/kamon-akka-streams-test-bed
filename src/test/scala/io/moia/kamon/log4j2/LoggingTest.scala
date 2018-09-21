package io.moia.kamon.log4j2

import com.typesafe.scalalogging.{Logger => ScalaLogger}
import io.moia.kamon.UnitTest
import java.util.UUID
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.{LogEvent, Logger, LoggerContext}
import scala.collection.convert.ImplicitConversions._

class LoggingTest extends UnitTest {

  "Logging" should {
    "have the MDC set via scala logging" in new Fixture {
      registerLogger()
      inContext(traceId) {
        ScalaLogger("SCALA").error(message)
        eventually(appender.allEvents should have size 1)
      }
      private val first: LogEvent = appender.allEvents.head
      first.getLoggerName shouldBe "SCALA"
      first.getMessage.getFormattedMessage shouldBe message
      first.getContextData.getValue("TraceId").asInstanceOf[String] shouldBe traceId
      unregisterLogger()
    }
  }

  class Fixture {
    val loggerId = UUID.randomUUID().toString
    val traceId = UUID.randomUUID().toString
    val message  = "Hello"

    val appender = new AbstractAppender(loggerId, null, null) { // Java API: null is allowed/expected here
      private[this] val events = List.newBuilder[LogEvent]

      override def append(event: LogEvent): Unit = synchronized(events += event.toImmutable)

      def allEvents = events.result()
    }

    def registerLogger(): Unit = {
      val ctx    = LogManager.getContext(false).asInstanceOf[LoggerContext]
      val config = ctx.getConfiguration
      appender.start()
      config.getAppenders
      config.addAppender(appender)
      val logger = LogManager.getRootLogger.asInstanceOf[Logger]
      // remove appenders to avoid error log on console
      logger.getAppenders.values().toList.foreach(logger.removeAppender)
      logger.addAppender(appender)
      ctx.updateLoggers()
    }

    def unregisterLogger(): Unit = {
      val logger = LogManager.getRootLogger.asInstanceOf[Logger]
      logger.removeAppender(appender)
    }
  }
}
