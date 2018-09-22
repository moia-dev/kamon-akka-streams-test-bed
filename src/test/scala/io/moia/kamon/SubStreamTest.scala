package io.moia.kamon
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalacheck.Gen

class SubStreamTest extends UnitTest with AkkaTest {

  "Sub Stream Flows" should {

    "maintain the context in a sub flow if a nested context is created" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .flatMapConcat { num  =>
              inContext(traceId + num) {
                Source.single(num).map(_ => currentTraceId())
              }
            }
            .map(inner => inner -> currentTraceId())
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue

          result should have size nums.size
          result.flatMap(_._1) should have size nums.size
          result.flatMap(_._2) should have size nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }

    "maintain the context in a sub flow via groupBy" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .groupBy(Int.MaxValue, identity)
            .map { num =>
              num -> setTraceId(num.toString)
            }
            .async
            .map { case (num, ctx) =>
              val result = currentTraceId()
              assert(result.contains(num.toString))
              ctx.close()
              result
            }
            .mergeSubstreams
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue

          result should have size nums.size
          result.flatten should contain allElementsOf nums.map(_.toString)
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }
  }
}
