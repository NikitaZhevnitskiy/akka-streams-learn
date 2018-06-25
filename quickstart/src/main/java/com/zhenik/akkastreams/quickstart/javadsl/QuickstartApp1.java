package com.zhenik.akkastreams.quickstart.javadsl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import scala.concurrent.ExecutionContextExecutor;

public class QuickstartApp1 {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("quickstart-app");
    Materializer mat = ActorMaterializer.create(system);
    ExecutionContextExecutor executionContextExecutor = system.dispatcher();



    final Source<Integer, NotUsed> source =
        Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

    final Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class).map(i -> i * 2);

    final Sink<Integer, NotUsed> sink = flow.to(Sink.foreach(System.out::println));

    final RunnableGraph<NotUsed> runnable =
        source.to(sink);
//    runnable.run(mat);

    // async boundary
    final RunnableGraph<NotUsed> runnableGraph = Source.range(1, 3)
        .map(x -> x + 1).async()
        .map(x -> x * 2)
        .to(printSink());
//    runnableGraph.run(mat);

    // connect the Source to the Sink, obtaining a RunnableGraph
    final Sink<Integer, CompletionStage<Integer>> sink1 =
        Sink.fold(0, (aggr, next) -> aggr + next);
    final RunnableGraph<CompletionStage<Integer>> runnable1 =
        Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).toMat(sink1, Keep.right());

//// get the materialized value of the FoldSink
//    final CompletionStage<Integer> sum1 = runnable.run(mat);
//    final CompletionStage<Integer> sum2 = runnable.run(mat);
  }

  private static Sink<Integer, NotUsed> printSink() {
    return Flow.of(Integer.class).to(Sink.foreach(System.out::println));
  }

}

