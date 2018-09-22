package io.moia.kamon

import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalacheck.Gen
import scala.concurrent.Future

class AsyncStreamTest extends UnitTest with AkkaTest {

  "Async Stream Flow" should {

    "maintain the context after an async boundary" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .async
            .map(_ => currentTraceId() )
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          result.flatten should have size nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }

    "maintain the context after a mapAsync" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .mapAsync(3)(_ => Future(currentTraceId())(system.dispatcher) )
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          result.flatten should have size nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }

    "maintain traceIds created during the flow" in {
      import mat.executionContext

      inContext("outer") {
        Source(0.until(100))
          .map( n => n -> setTraceId(n.toString) )
          .mapAsync(5) { case (n, scope) =>
            Future {
              assert( currentTraceId().contains(n.toString), s"In map async for n = $n: ${currentTraceId()} != $n" )
              (n + 1) -> scope
            }
          }
          .runFold(0) { case (acc, (m, scope)) =>
            val n = m - 1
            assert( currentTraceId().contains(n.toString), s"In run fold for n = $n: ${currentTraceId()} != $n" )
            scope.close()
            acc + m
          }
      }.futureValue shouldBe 5050
    }
  }
}
