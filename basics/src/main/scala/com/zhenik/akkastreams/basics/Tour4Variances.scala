package com.zhenik.akkastreams.basics

class Foo[+A] // A covariant class / may appear in lower bounds of method type params
class Bar[-A] // A contravariant class / may appear in upper bounds of method
class Baz[A]  // An invariant class


  /** Covariant */
abstract class Animal1 {
  def name: String
}
case class Cat1(name: String) extends Animal1
case class Dog1(name: String) extends Animal1

object Tour4Variances_Covariant extends App {
  def printAnimalNames(animals: List[Animal1]): Unit = {
    animals.foreach { animal =>
      println(animal.name)
    }
  }
  val cats: List[Cat1] = List(Cat1("Whiskers"), Cat1("Tom"))
  val dogs: List[Dog1] = List(Dog1("Fido"), Dog1("Rex"))
  printAnimalNames(cats)
  // Whiskers
  // Tom
  printAnimalNames(dogs)
  // Fido
  // Rex
}


  /** Covariant */

abstract class Printer[-A] {
  def print(value: A): Unit
}
class AnimalPrinter extends Printer[Animal1] {
  def print(animal: Animal1): Unit =
    println("The animal's name is: " + animal.name)
}

class CatPrinter extends Printer[Cat1] {
  def print(cat: Cat1): Unit =
    println("The cat's name is: " + cat.name)
}

object Tour4Variances_Contravariance extends App {
  val myCat: Cat1 = Cat1("Boots")

  def printMyCat(printer: Printer[Cat1]): Unit = {
    printer.print(myCat)
  }

  val catPrinter: Printer[Cat1] = new CatPrinter
  val animalPrinter: Printer[Animal1] = new AnimalPrinter

  printMyCat(catPrinter)
  printMyCat(animalPrinter)
}
