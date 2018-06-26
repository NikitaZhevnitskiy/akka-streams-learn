package com.zhenik.akkastreams.quickstart

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent._

final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] = body.split(" ").collect {
    case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
  }.toSet
}

object QuickstartApp2 extends App {

  // Akka
  implicit val system: ActorSystem = ActorSystem("quickstart-app")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  val akkaTag = Hashtag("#akka")

  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)


  tweets
    .map(_.hashtags) // Get all sets of hashtags ...
    .reduce(_ ++ _) // ... and reduce them to a single set, removing duplicates across all tweets
    .mapConcat(identity) // Flatten the stream of tweets to a stream of hashtags
    .map(_.name.toUpperCase) // Convert all hashtags to upper case
    .runWith(Sink.foreach(println)) // Attach the Flow to a Sink that will finally print the hashtags

  // $FiddleDependency org.akka-js %%% akkajsactorstream % 1.2.5.1

  val writeAuthors: Sink[Author, NotUsed] = Flow[Author].to(Sink.foreach(println(_)))
  val writeHashtags: Sink[Hashtag, NotUsed] = Flow[Hashtag].to(Sink.foreach(println(_)))
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Tweet](2))
    tweets ~> bcast.in
    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
    ClosedShape
  })
  g.run()

  tweets
    .buffer(10, OverflowStrategy.dropHead)
    .map(slowComputation)
    .runWith(Sink.ignore)

  def slowComputation(tweet: Tweet):Tweet = tweet


  val tweetsInMinuteFromNow = tweets

  val sumSink = Sink.fold[Int, Int](0)(_ + _)
  val counterRunnableGraph: RunnableGraph[Future[Int]] =
    tweetsInMinuteFromNow
      .filter(_.hashtags contains akkaTag)
      .map(t => 1)
      .toMat(sumSink)(Keep.right)

  // materialize the stream once in the morning
  val morningTweetsCount: Future[Int] = counterRunnableGraph.run()
  // and once in the evening, reusing the flow
  val eveningTweetsCount: Future[Int] = counterRunnableGraph.run()

}
