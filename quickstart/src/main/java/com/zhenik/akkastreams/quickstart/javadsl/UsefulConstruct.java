package com.zhenik.akkastreams.quickstart.javadsl;

import akka.NotUsed;
import akka.dispatch.Futures;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class UsefulConstruct {
  public static void main(String[] args) {
    //
    // Create a source from an Iterable
    List<Integer> list = new LinkedList<Integer>();
    list.add(1);
    list.add(2);
    list.add(3);
    Source.from(list);

// Create a source form a Future
    Source.fromFuture(Futures.successful("Hello Streams!"));

// Create a source from a single element
    Source.single("only one element");

// an empty source
    Source.empty();

// Sink that folds over the stream and returns a Future
// of the final result in the MaterializedMap
    Sink.fold(0, (Integer aggr, Integer next) -> aggr + next);

// Sink that returns a Future in the MaterializedMap,
// containing the first element of the stream
    Sink.head();

// A Sink that consumes a stream without doing anything with the elements
    Sink.ignore();

// A Sink that executes a side-effecting call for every element of the stream
    Sink.foreach(System.out::println);




// Explicitly creating and wiring up a Source, Sink and Flow
    Source.from(Arrays.asList(1, 2, 3, 4))
        .via(Flow.of(Integer.class).map(elem -> elem * 2))
        .to(Sink.foreach(System.out::println));

// Starting from a Source
    final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4))
        .map(elem -> elem * 2);
    source.to(Sink.foreach(System.out::println));

// !!! Broadcast inline
    final Sink<Integer, NotUsed> sink = Flow.of(Integer.class)
        .map(elem -> elem * 2).alsoTo(Sink.foreach(System.out::println)).to(Sink.ignore());

    Source.from(Arrays.asList(1, 2, 3, 4)).to(sink);
  }
}
