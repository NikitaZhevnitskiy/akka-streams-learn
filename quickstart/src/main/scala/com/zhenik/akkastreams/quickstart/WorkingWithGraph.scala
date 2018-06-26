package com.zhenik.akkastreams.quickstart

import akka.NotUsed
import akka.actor.{Actor, ActorSystem}
import akka.stream._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip, ZipWith}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import org.scalatest.{FunSpecLike, _}

object WorkingWithGraph extends App with FunSpecLike  with Matchers {

  // Akka
  implicit val system: ActorSystem = ActorSystem("quickstart-app")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  // !!! That's amazing
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val in: Source[Int, NotUsed] = Source(1 to 10)
    val out1 = Flow[Int].to(Sink.foreach(element => println(s"out 1: $element")))
    val out2 = Flow[Int].to(Sink.foreach(element => println(s"out 2: $element")))

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    val f1 = Flow[Int].map(_ + 1)
    val f2 = Flow[Int].map(_ + 2)
    val f3 = Flow[Int].map(_ + 3)
    val f4 = Flow[Int].map(_ + 4)

    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out1
    bcast ~> f4 ~> merge
    ClosedShape
  })

  g.run()


  // PARTITIAL GRAPH
  val pickMaxOfThree = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zip1 = b.add(ZipWith[Int, Int, Int](math.max))
    val zip2 = b.add(ZipWith[Int, Int, Int](math.max))
    zip1.out ~> zip2.in0

    UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
  }
  val resultSink = Sink.head[Int]
  val maxGraph = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b => sink =>
    import GraphDSL.Implicits._

    // importing the partial graph will return its shape (inlets & outlets)
    val pm3 = b.add(pickMaxOfThree)

    Source.single(1) ~> pm3.in(0)
    Source.single(2) ~> pm3.in(1)
    Source.single(6) ~> pm3.in(2)
    pm3.out ~> sink.in
    ClosedShape
  })
  val max: Future[Int] = maxGraph.run()
  Await.result(max, 300.millis) should equal(6)

  // SHAPES source
  val pairs = Source.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    // prepare graph elements
    val zip = b.add(Zip[Int, Int]())
//    def ints = Source.fromIterator(() => Iterator.from(3))
    def ints = Source(1 to 7)
    // connect the graph
    ints.filter(_ % 3 != 0) ~> zip.in0
    ints.filter(_ % 3 == 0) ~> zip.in1

    // expose port
    SourceShape(zip.out)
  })

  val firstPair: Future[(Int, Int)] = pairs.runWith(Sink.head)
  Await.result(firstPair, 300.millis) should equal( (1,3) )
  val wholeExample = pairs.runWith(Flow[(Int, Int)].to(Sink.foreach(a => println(s"SHAPES_SOURCE: $a"))))

  // SHAPES Flow
  val pairUpWithToString =
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // prepare graph elements
      val broadcast = b.add(Broadcast[Int](2))
      val zip = b.add(Zip[Int, String]())

      // connect the graph
      broadcast.out(0).map(identity) ~> zip.in0
      broadcast.out(1).map(_.toString) ~> zip.in1

      // expose ports
      FlowShape(broadcast.in, zip.out)
    })
  pairUpWithToString.runWith(Source(1 to 5), Sink.foreach(a => println(s"SHAPES_FLOW: int[${a._1}] string[${a._2}]")))

  // Simple API Source
  val sourceOne = Source(1 to 3)
  val sourceTwo = Source(1 to 2)
  val merged = Source.combine(sourceOne, sourceTwo)(Merge(_))
  val mergedResult: Future[Int] = merged.runWith(Sink.fold(0)(_ + _))
  mergedResult.onComplete(res => println(s"MERGED_RESULT: $res"))

  // Simple API Sink
//  val sendRmotely = Sink.actorRef(Actor.noSender, "Done")
//  val localProcessing = Sink.foreach[Int](_ => /* do something usefull */ ())
//  val sink = Sink.combine(sendRmotely, localProcessing)(Broadcast[Int](_))
//  Source(List(0, 1, 2)).runWith(sink)

}
