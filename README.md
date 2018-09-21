# Kamon Akka Streams Test Bed

## Run

The tests are available as regular unit tests and can be simply executed via: 

```scala
$> sbt test
```

## Context propagation

### 1) Use the Kamon Context as MDC for logging
See io.moia.kamon.log4j2.KamonContextDataInjector

At MOIA we use the Kamon to set a MDC logging context. 
All log statements will have the context available.
The LoggingTest makes sure, that the MDC is set.
Expectation: the context can be used to set a MDC for logging.
Status: Works!

### 2) Simple stream processing
See io.moia.kamon.SimpleStreamTest

This test uses the most basic akka stream functionality.
It combines a source with a map operation that flows to a sink.
Expectation:  The context is propagated during the flow. 
Status: not working.


### 3) Sub stream processing
See io.moia.kamon.SubStreamTest

This test uses 2 common ways of creating sub-flows.
A new context is created in the sub-flow that is later on combined with the outer context.
Expectation: Both inner as well as outer context is propagated during the flow. 
Status: not working.

### 4) Async stream processing
See io.moia.kamon.AsyncStreamTest

This test uses 2 different ways of creating an async boundary.
The context is gathered after the async boundary.
Expectation: the context is propagated. 
Status: not working.

### 5) Custom stage 
See io.moia.kamon.CustomStageTest

I implemented a simple custom graph stage for demo purposes: io.moia.kamon.ExampleCustomStage

This test uses a flow with this custom graph stage.
The context is gathered after elements are flowing from the custom stage.
Expectation: the context is propagated and available after the custom stage. 
Status: not working.

### 6) Graph stream processing
See io.moia.kamon.GraphTest

This test uses the GraphDSL to construct a graph where each element flows through several edges of the graph.
The context is gathered for all different edges.
Expectation: the context is propagated through all edges. 
Status: not working.

