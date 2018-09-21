package io.moia.kamon
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.moia.kamon.log4j2.Logging
import scala.collection.mutable

final class ExampleCustomStage[I] extends GraphStage[FlowShape[I, I]] with Logging {

  val in: Inlet[I]   = Inlet[I]("DummyBuffer.in")
  val out: Outlet[I] = Outlet[I]("DummyBuffer.out")
  override val shape: FlowShape[I, I] = new FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    private[this] var buffer        = mutable.Queue.empty[I]

    override def preStart(): Unit = {
      // this stage should keep going, even if upstream is finished
      setKeepGoing(true)
      pull(in)
    }

    override def postStop(): Unit = {
      logger.info(s"postStop: ${buffer.size} requests are currently in buffer")
      super.postStop()
    }

    setHandler(
      in,
      new InHandler {
        override def onPush(): Unit = {
          val element = grab(in)
            buffer += element
            logger.info(s"onPush: Add Element to Buffer: $element")
            if (isAvailable(out)) {
              val topElement = buffer.dequeue()
              logger.info(s"onPush: Push Element to out: $topElement")
              push(out, topElement)
            }
          // signal demand
          pull(in)
        }

        override def onUpstreamFinish(): Unit = {
          logger.info("Upstream is finished!")
          // Only finish the stage if there are no outstanding messages
          // If there are outstanding messages, the receive handler will complete the stage.
          if (buffer.isEmpty) completeStage()
        }
      }
    )

    setHandler(
      out,
      new OutHandler {
        override def onPull(): Unit = {
          if (buffer.nonEmpty) {
            val topElement = buffer.dequeue()
            logger.info(s"onPull: Push Element to out: $topElement")
            push(out, topElement)
          }
          pullIfNecessary()
        }
      }
    )

    def pullIfNecessary(): Unit =
      if (isClosed(in) && buffer.isEmpty) {
        completeStage()
      } else if (!hasBeenPulled(in)) {
        pull(in)
      }
  }
}
