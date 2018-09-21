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
  }
}
