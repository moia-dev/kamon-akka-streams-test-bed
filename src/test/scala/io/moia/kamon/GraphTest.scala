package io.moia.kamon

import akka.NotUsed
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalacheck.Gen

class GraphTest extends UnitTest with AkkaTest {

  "A Flow with a custom stage" should {

    "maintain the context during a graph" in {
      forAll(nonEmptyStringGen, Gen.listOf(Gen.posNum[Int])) { (traceId, nums) =>
        inContext(traceId) {
          currentTraceId() shouldBe Some(traceId)
          val result = Source(nums)
            .map{ num =>
              setTraceId(num.toString)
              num
            }
            .via(broadcastAndMerge(Flow.fromFunction((_: Int) => currentTraceId())))
            .toMat(Sink.seq)(Keep.right)
            .run()
            .futureValue
          // each element is broadcasted and merged 5 times --> expect 5 times size
          result should have size 5 * nums.size
          result.flatten should have size 5 * nums.size
          result.flatten should contain allElementsOf nums.map(_.toString)
          currentTraceId() shouldBe Some(traceId)
        }
      }
    }

    "maintain the context during a graph with source queue" in {
      forAll(Gen.listOf(Gen.posNum[Int])) { nums =>

        val (queue, sink) = Source.queue[Int](nums.size, OverflowStrategy.fail)
          .via(broadcastAndMerge(Flow.fromFunction((_: Int) => currentTraceId())))
          .toMat(TestSink.probe)(Keep.both)
          .run()

        inContext("outer") {
          nums.map{ num =>
            setTraceId(num.toString)
            queue.offer(num)
          }.map(_.futureValue)
          sink.request(5 * nums.size)
          val result = sink.expectNextN(5 * nums.size)
          // each element is broadcasted and merged 5 times --> expect 5 times size
          result.flatten should have size 5 * nums.size
          result.flatten should contain allElementsOf nums.map(_.toString)
        }
      }
    }
  }


  def broadcastAndMerge[I,O] (flow: Flow[I, O, NotUsed]): Flow[I, O, NotUsed] =
    Flow.fromGraph[I, O, NotUsed](GraphDSL.create(flow) { implicit builder => flow =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[I](5))
      val merge = builder.add(Merge[I](5))
      def fn = builder.add(Flow[I].async)

      // dummy graph to illustrate, that the element can take several paths
      broadcast ~> fn ~> merge
      broadcast ~> fn ~> merge
      broadcast ~> fn ~> merge
      broadcast ~> fn ~> merge
      broadcast ~> fn ~> merge
                         merge ~>  flow

      FlowShape(broadcast.in, flow.out)
    })
}
