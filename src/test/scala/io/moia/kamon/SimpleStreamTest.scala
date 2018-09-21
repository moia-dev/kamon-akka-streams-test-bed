package io.moia.kamon
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalacheck.Gen

class SimpleStreamTest extends UnitTest with AkkaTest {

  "Simple Stream Flow" should {

    "maintain the context during a simple map" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .map(_ => currentTraceId() )
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          result.flatten should have size nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }

    "maintain the context if a nested context is created" in {
      forAll(nonEmptyStringGen, nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nested, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .map(_ => inContext(nested)(currentTraceId()) -> currentTraceId())
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          result.flatMap(_._1) should have size nums.size
          result.flatMap(_._2) should have size nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }
  }
}
