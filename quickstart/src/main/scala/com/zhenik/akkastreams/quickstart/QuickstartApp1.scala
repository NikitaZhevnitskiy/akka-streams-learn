package com.zhenik.akkastreams.quickstart

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent._


object QuickstartApp1 extends App {
  println(s"yo ma hat")

  // Akka
  implicit val system: ActorSystem = ActorSystem("quickstart-app")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)
  // not terminated
  //  source.runForeach(println(_))


  // understanding of .scan()
  val done: Future[Done] = source.scan(0)( (a,b) => {
    print(s"$a + $b = ")
    a+b
  }).runForeach(println(_))

  // require dispatcher for termination
  // implicit val ec = system.dispatcher
  done.onComplete(_ => system.terminate())




  val factorials = source.scan(BigInt(1))((acc, next)=> acc * next)
  val result: Future[IOResult] =
    factorials
      .map(num ⇒ ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

}
