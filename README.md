# Kamon Akka Streams Test Bed

## Run

The tests are available as regular unit tests and can be simply executed via: 

```scala
$> sbt test
```

## Context propagation

### 1) Use the Kamon Context as MDC for logging
See [io.moia.kamon.log4j2.LoggingTest](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/test/scala/io/moia/kamon/log4j2/LoggingTest)



At MOIA we use Kamon to set a MDC logging context. 
A custom data injector [io.moia.kamon.log4j2.KamonContextDataInjector](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/main/scala/io/moia/kamon/log4j2/Logging.scala)
is used to achieve this goal.
If a context is available, all log statements will print a TraceId to the console.
The LoggingTest makes sure, that the MDC is set.

Expectation: the context can be used to set a MDC for logging.

**Status: Works!**

### 2) Simple stream processing
See [io.moia.kamon.SimpleStreamTest](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/test/scala/io/moia/kamon/SimpleStreamTest.scala)

This test uses the most basic akka stream functionality.
It combines a source with a map operation that flows to a sink.

Expectation:  The context is propagated during the flow. 

**Status: Works!**


### 3) Sub stream processing
See [io.moia.kamon.SubStreamTest](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/test/scala/io/moia/kamon/SubStreamTest.scala)

This test uses 2 common ways of creating sub-flows.
A new context is created in the sub-flow that is later on combined with the outer context.

Expectation: Both inner as well as outer context is propagated during the flow. 

**Status: Works!**

### 4) Async stream processing
See [io.moia.kamon.AsyncStreamTest](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/test/scala/io/moia/kamon/AsyncStreamTest.scala)

This test uses 2 different ways of creating an async boundary.
The context is gathered after the async boundary.

Expectation: the context is propagated. 

**Status: not working.**

### 5) Custom stage 
See [io.moia.kamon.CustomStageTest](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/test/scala/io/moia/kamon/CustomStageTest.scala)

I implemented a simple custom graph stage for demo purposes: io.moia.kamon.ExampleCustomStage

This test uses a flow with this custom graph stage.
The context is gathered after elements are flowing from the custom stage.

Expectation: the context is propagated and available after the custom stage. 

**Status: Works!**

### 6) Graph stream processing
See [io.moia.kamon.GraphTest](https://github.com/moia-dev/kamon-akka-streams-test-bed/blob/master/src/test/scala/io/moia/kamon/GraphTest.scala)

This test uses the GraphDSL to construct a graph where each element flows through several edges of the graph.
The context is gathered for all different edges.

Expectation: the context is propagated through all edges. 

**Status: not working.**

