package com.zhenik.akkastreams.quickstart.javadsl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ClosedShape;
import akka.stream.Materializer;
import akka.stream.Outlet;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.Arrays;
import scala.concurrent.ExecutionContextExecutor;

public class WorkingWithGraph {
  public static void main(String[] args) {
    //
    ActorSystem system = ActorSystem.create("quickstart-app");
    Materializer mat = ActorMaterializer.create(system);
    ExecutionContextExecutor executionContextExecutor = system.dispatcher();

    final Source<Integer, NotUsed> in = Source.from(Arrays.asList(1, 2, 3, 4, 5));
    final Sink<Integer, NotUsed> sink =
        Flow.of(Integer.class).to(Sink.foreach(System.out::println));
    final Flow<Integer, Integer, NotUsed> f1 = Flow.of(Integer.class).map(elem -> elem + 1);
    final Flow<Integer, Integer, NotUsed> f2 = Flow.of(Integer.class).map(elem -> elem + 2);
    final Flow<Integer, Integer, NotUsed> f3 = Flow.of(Integer.class).map(elem -> elem + 3);
    final Flow<Integer, Integer, NotUsed> f4 = Flow.of(Integer.class).map(elem -> elem + 4);

    final RunnableGraph<NotUsed> result =
        RunnableGraph.fromGraph(
            GraphDSL // create() function binds sink, out which is sink's out port and builder DSL
                .create( // we need to reference out's shape in the builder DSL below (in to()
                         // function)
                    sink, // previously created sink (Sink)
                    (builder, out) -> { // variables: builder (GraphDSL.Builder) and out (SinkShape)
                      final UniformFanOutShape<Integer, Integer> bcast =
                          builder.add(Broadcast.create(2));
                      final UniformFanInShape<Integer, Integer> merge =
                          builder.add(Merge.create(2));

                      final Outlet<Integer> source = builder.add(in).out();
                      builder
                          .from(source)
                          .via(builder.add(f1))
                          .viaFanOut(bcast)
                          .via(builder.add(f2))
                          .viaFanIn(merge)
                          .via(builder.add(f3))
                          .to(out); // to() expects a SinkShape
                      builder.from(bcast).via(builder.add(f4)).toFanIn(merge);
                      return ClosedShape.getInstance();
                    }));

    result.run(mat);
  }
}
