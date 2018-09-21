package io.moia.kamon.log4j2

import java.util

import com.typesafe.scalalogging.StrictLogging
import kamon.Kamon
import kamon.context.Key
import org.apache.logging.log4j.core.ContextDataInjector
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.util._

/**
  * The default logging trait.
  */
trait Logging extends StrictLogging

object Logging {

  // Defines the key for accessing the trace id in kamon context
  val TraceId: Key[Option[String]] = Key.broadcastString("TraceId")

  // All context keys that should be added to logging context.
  val ContextKeys: Seq[Key[Option[String]]] = Seq(TraceId)
}

/**
  * Log4j2 custom data injector.
  * This injector makes the kamon context available to the logging context.
  * Note:
  *   The methods listed here are called by the calling thread of the logging function.
  *   The returned StringMap should eagerly fetch all values needed, since evaluation happens on another thread.
  *
  * See https://logging.apache.org/log4j/2.x/manual/extending.html#Custom_ContextDataInjector
  */
class KamonContextDataInjector extends ContextDataInjector {
  import Logging._

  /**
    * This method is called before each and every log event.
    * For efficiency reasons we reuse the given StringMap.
    * According to the documentation, this string map already contains the given properties when this method is called.
    */
  override def injectContextData(props: util.List[Property], reusable: StringMap): StringMap = {
    ContextKeys.foreach { key =>
      Kamon.currentContext().get(key).foreach(value => reusable.putValue(key.name, value))
    }
    reusable
  }

  override def rawContextData(): ReadOnlyStringMap = {
    val map = new util.HashMap[String, String]()
    ContextKeys.foreach { key =>
      Kamon.currentContext().get(key).foreach(value => map.put(key.name, value))
    }
    new SortedArrayStringMap(map)
  }
}
