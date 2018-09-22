package io.moia.kamon

import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import kamon.context.Storage.Scope
import org.scalacheck.Gen

class CustomStageTest extends UnitTest with AkkaTest {

  "A Flow with a custom stage" should {

    "maintain the context with a custom stage" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .via(Flow.fromGraph(new ExampleCustomStage[Int]))
            .map(_ => currentTraceId() )
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          result should have size nums.size
          result.flatten should have size nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }


    "maintain a new context created during the flow with a custom stage" in {
      forAll(nonEmptyStringGen) { traceId =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(1.to(1000))
            .map(num => num -> setTraceId(num.toString))
            .via(Flow.fromGraph(new ExampleCustomStage[(Int, Scope)]))
            .map { case (num, scope) =>
              val traceId = currentTraceId()
              traceId shouldBe Some(num.toString)
              scope.close()
              traceId
            }
            .toMat(TestSink.probe)(Keep.right)
            .run()

          1.to(10).foreach { num =>
            val traceIds = result.request(100).expectNextN(100).flatten
            traceIds should have size 100
            traceIds should contain allElementsOf 1.to(100).map(_ + num * 100).map(_.toString)
          }
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }
  }
}
