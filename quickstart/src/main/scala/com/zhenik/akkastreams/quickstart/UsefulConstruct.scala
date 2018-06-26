package com.zhenik.akkastreams.quickstart

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future


object UsefulConstruct extends App {

  // Create a source from an Iterable
  Source(List(1, 2, 3))

  // Create a source from a Future
  Source.fromFuture(Future.successful("Hello Streams!"))

  // Create a source from a single element
  Source.single("only one element")

  // an empty source
  Source.empty

  // Sink that folds over the stream and returns a Future
  // of the final result as its materialized value
  Sink.fold[Int, Int](0)(_ + _)

  // Sink that returns a Future as its materialized value,
  // containing the first element of the stream
  Sink.head

  // A Sink that consumes a stream without doing anything with the elements
  Sink.ignore

  // A Sink that executes a side-effecting call for every element of the stream
  Sink.foreach[String](println(_))



  // Explicitly creating and wiring up a Source, Sink and Flow
  Source(1 to 6).via(Flow[Int].map(_ * 2)).to(Sink.foreach(println(_)))

  // Starting from a Source
  val source = Source(1 to 6).map(_ * 2)
  source.to(Sink.foreach(println(_)))

  // Starting from a Sink
  val sink: Sink[Int, NotUsed] = Flow[Int].map(_ * 2).to(Sink.foreach(println(_)))
  Source(1 to 6).to(sink)

  // !!! Broadcast to a sink inline
  val otherSink: Sink[Int, NotUsed] =
    Flow[Int].alsoTo(Sink.foreach(println(_))).to(Sink.ignore)
  Source(1 to 6).to(otherSink)
}
