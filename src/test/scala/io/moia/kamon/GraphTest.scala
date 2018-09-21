package io.moia.kamon

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, Sink, Source}
import org.scalacheck.Gen

class GraphTest extends UnitTest with AkkaTest {

  "A Flow with a custom stage" should {

    "maintain the context during a graph" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .via(broadcastAndMerge(Flow.fromFunction((_: Int) => currentTraceId())))
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          // each element is broadcasted and merged 5 times --> expect 5 times size
          result should have size 5 * nums.size
          result.flatten should have size 5 * nums.size
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }
  }


  def broadcastAndMerge[I,O] (flow: Flow[I, O, NotUsed]): Flow[I, O, NotUsed] =
    Flow.fromGraph[I, O, NotUsed](GraphDSL.create(flow) { implicit builder => flow =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[I](5))
      val merge = builder.add(Merge[I](5))

      // dummy graph to illustrate, that the element can take several paths
      broadcast ~> merge
      broadcast ~> merge
      broadcast ~> merge
      broadcast ~> merge
      broadcast ~> merge

      merge ~> flow
      FlowShape(broadcast.in, flow.out)
    })
}
