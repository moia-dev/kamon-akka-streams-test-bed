package io.moia.kamon
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestKit
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import io.moia.kamon.log4j2.Logging
import org.scalacheck.Gen
import org.scalatest.{BeforeAndAfterAll, Matchers, Suite, WordSpec, WordSpecLike}
import org.scalatest.concurrent.{Eventually, PatienceConfiguration, ScalaFutures}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.time.{Second, Span}

/**
  * Base trait for all unit tests.
  */
trait UnitTestLike
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with Eventually
    with StrictLogging
    with GeneratorDrivenPropertyChecks
    with Logging
    with TraceIdContext {

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 10)
  val nonEmptyStringGen: Gen[String] = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)
}
abstract class UnitTest extends WordSpec with UnitTestLike

/**
  * Akka unit tests. The actor system along with
  * its configuration is established just once. Correspondingly the
  * actor system is terminated at the end of all of the tests.
  */
trait AkkaTest extends BeforeAndAfterAll with PatienceConfiguration { this: Suite =>

  implicit lazy val system: ActorSystem = ActorSystem(suiteName, config)
  implicit lazy val mat: Materializer   = ActorMaterializer()

  protected def config: Config =
    ConfigFactory.load()

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  // Adjust the default patience configuration for all akka based tests
  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(1, Second)))
}

