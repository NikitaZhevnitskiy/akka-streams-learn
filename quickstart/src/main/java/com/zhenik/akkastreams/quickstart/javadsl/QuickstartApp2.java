package com.zhenik.akkastreams.quickstart.javadsl;


import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ClosedShape;
import akka.stream.FlowShape;
import akka.stream.Materializer;
import akka.stream.SinkShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class QuickstartApp2 {

  public static void main(String[] args) {
    //
    final ActorSystem system = ActorSystem.create("reactive-tweets");
    final Materializer mat = ActorMaterializer.create(system);
    final Source<Tweet, NotUsed> tweets = Source.from(getTwits());

//        final Source<Author, NotUsed> authors =
//            tweets
//                .filterNot(Objects::isNull)
//                .filter(t -> t.hashtags().contains(AKKA))
//                .map(t -> t.author);

    //    authors.runWith(Sink.foreach(System.out::println), mat);
    //    final Source<Hashtag, NotUsed> hashtags =
    //        tweets.mapConcat(t -> new ArrayList<Hashtag>(t.hashtags()));

    Sink<Author, NotUsed> writeAuthors = Flow.of(Author.class).to(Sink.foreach(System.out::println));
    Sink<Hashtag, NotUsed> writeHashtags = Flow.of(Hashtag.class).to(Sink.foreach(System.out::println));
    RunnableGraph.fromGraph(GraphDSL.create(b -> {
      final UniformFanOutShape<Tweet, Tweet> bcast = b.add(Broadcast.create(2));
      final FlowShape<Tweet, Author> toAuthor =
          b.add(Flow.of(Tweet.class).map(t -> t.author));
      final FlowShape<Tweet, Hashtag> toTags =
          b.add(Flow.of(Tweet.class).mapConcat(t -> new ArrayList<Hashtag>(t.hashtags())));
      final SinkShape<Author> authors = b.add(writeAuthors);
      final SinkShape<Hashtag> hashtags = b.add(writeHashtags);

      b.from(b.add(tweets)).viaFanOut(bcast).via(toAuthor).to(authors);
      b.from(bcast).via(toTags).to(hashtags);
      return ClosedShape.getInstance();
    })).run(mat);


  }

  public static class Author {
    public final String handle;

    public Author(String handle) {
      this.handle = handle;
    }

    @Override
    public String toString() {
      return "[Author: "+this.handle + "]";
    }

  }

  public static class Hashtag {
    public final String name;

    public Hashtag(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "[Hashtag#: "+this.name + "]";
    }
  }

  public static class Tweet {
    public final Author author;
    public final long timestamp;
    public final String body;

    public Tweet(Author author, long timestamp, String body) {
      this.author = author;
      this.timestamp = timestamp;
      this.body = body;
    }

    public Set<Hashtag> hashtags() {
      return Arrays.stream(body.split(" "))
          .filter(a -> a.startsWith("#"))
          .map(Hashtag::new)
          .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
      return "[Tweet: "+this.body + "]";
    }

    // ...
  }

  public static List<Tweet> getTwits() {
    return Arrays.asList(
        new Tweet(new Author("rolandkuhn"), Instant.now().getEpochSecond(), "#akka rocks!"),
        new Tweet(new Author("patriknw"), Instant.now().getEpochSecond(), "#akka !"),
        new Tweet(new Author("bantonsson"), Instant.now().getEpochSecond(), "#akka !"),
        new Tweet(new Author("drewhk"), Instant.now().getEpochSecond(), "#akka !"),
        new Tweet(new Author("ktosopl"), Instant.now().getEpochSecond(), "#akka on the rocks!"),
        new Tweet(new Author("mmartynas"), Instant.now().getEpochSecond(), "wow #akka !"),
        new Tweet(new Author("akkateam"), Instant.now().getEpochSecond(), "#akka rocks!"),
        new Tweet(new Author("bananaman"), Instant.now().getEpochSecond(), "#bananas rock!"),
        new Tweet(new Author("appleman"), Instant.now().getEpochSecond(), "#apples rock!"),
        new Tweet(new Author("appleman"), Instant.now().getEpochSecond(), "we compared #apples to #oranges!")
    );
  }

  public static final Hashtag AKKA = new Hashtag("#akka");
}
