package com.zhenik.akkastreams.basics

abstract class Animal { def name: String }

abstract class Pet extends Animal {}

class Cat extends Pet {
  override def name: String = "Cat"
}

class Dog extends Pet {
  override def name: String = "Dog"
}

class Lion extends Animal {
  override def name: String = "Lion"
}

// An upper type bound `T <: A` declares that type variable `T` refers to a subtype of type `A`
class PetContainer[P <: Pet](p: P) {def pet: P = p}

object Tour5TypeBounds_UpperBound extends App {

  val dogContainer = new PetContainer[Dog](new Dog)
  val catContainer = new PetContainer[Cat](new Cat)
//  val lionContainer = new PetContainer[Lion](new Lion)
//                           ^this would not compile
}


trait Node[+B] {
  def prepend[U >: B](elem: U): Node[U]
}

case class ListNode[+B](h: B, t: Node[B]) extends Node[B] {
  def prepend[U >: B](elem: U): ListNode[U] = ListNode(elem, this)
  def head: B = h
  def tail: Node[B] = t
}

case class Nil[+B]() extends Node[B] {
  def prepend[U >: B](elem: U): ListNode[U] = ListNode(elem, this)
}

trait Bird
case class AfricanSwallow() extends Bird
case class EuropeanSwallow() extends Bird

object Tour5TypeBounds_LowerBound extends App {
  val africanSwallowList= ListNode[AfricanSwallow](AfricanSwallow(), Nil())
  val birdList: Node[Bird] = africanSwallowList
  birdList.prepend(new EuropeanSwallow)
}