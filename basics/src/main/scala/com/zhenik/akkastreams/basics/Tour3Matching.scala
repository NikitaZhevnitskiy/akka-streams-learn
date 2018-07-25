package com.zhenik.akkastreams.basics

import scala.util.Random


abstract class Device
case class Phone(model: String) extends Device{ def screenOff = "Turning screen off"}
case class Computer(model: String) extends Device { def screenSaverOn = "Turning screen saver on..." }

object Tour3Matching extends App {

  val deviceOff = (device: Device) => {
    device match {
      case p: Phone => p.screenOff
      case c: Computer => c.screenSaverOn
    }
  }
  println(deviceOff(Phone("c3010")))



  val customer1ID = CustomerID("Sukyoung")  // Sukyoung--23098234908
  customer1ID match {
    case CustomerID(name) => println(name)  // prints Sukyoung
    case _ => println("Could not extract a CustomerID")
  }


  val customer2ID = CustomerID("Nico")
  val CustomerID(name) = customer2ID
  println(name)  // prints Nico


}


object CustomerID {
  def apply(name: String) = s"$name--${Random.nextLong}"
  def unapply(customerID: String): Option[String] = {
    val stringArray: Array[String] = customerID.split("--")
    if (stringArray.tail.nonEmpty) Some(stringArray.head) else None
  }
}

