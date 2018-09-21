package io.moia.kamon
import io.moia.kamon.log4j2.Logging
import kamon.Kamon

trait TraceIdContext {

  /**
    * Execute given function inside a context with given traceId.
    */
  def inContext[T](traceId: String)(fn: => T): T = Kamon.withContextKey(Logging.TraceId, Some(traceId))(fn)

  /**
    *  Return the current traceId.
    */
  def currentTraceId(): Option[String] = Kamon.currentContext().get(Logging.TraceId)
}
