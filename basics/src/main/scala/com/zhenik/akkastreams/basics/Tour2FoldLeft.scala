package com.zhenik.akkastreams.basics

object Tour2FoldLeft extends App {

  // def foldLeft[B](z: B)(op: (B,A) => B) : B
  val list: List[Int] = (1 to 10).toList
  println(list)
  val res1 = list.foldLeft(0)((m,n)=> m+n)
  val res2 = list.foldLeft(0)(_+_)
  println(res1)
  println(res2)


  val numberFunc = list.foldLeft(List[Int]())_

  val squares = numberFunc((xs, x) => xs:+ x*x)
  println(squares.toString()) // List(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)

  val cubes = numberFunc((xs, x) => xs:+ x*x*x)
  println(cubes.toString())  // List(1, 8, 27, 64, 125, 216, 343, 512, 729, 1000)


  val newList = list:+5
  println(newList)
}
