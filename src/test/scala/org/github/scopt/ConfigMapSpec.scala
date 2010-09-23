package org.github.scopt

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class ConfigMapSpec extends FunSuite with ShouldMatchers {

  case class TestConfig() extends ConfigMap {
    def a = get[String]("a") getOrElse ""
    def b = get[Int]("b") getOrElse -1
    def c = get[Double]("c") getOrElse Double.NaN
  }

  test("default value string") {
    case class TestConfig() extends ConfigMap {
      def a = get[String]("a") getOrElse ""
    }
    TestConfig().a should be ("")
  }

  test("default value int") {
    case class TestConfig() extends ConfigMap {
      def a = get[Int]("a") getOrElse -1
    }
    TestConfig().a should be (-1)
  }

  test("default value double") {
    case class TestConfig() extends ConfigMap {
      def a = get[Double]("a") getOrElse Double.NaN
    }
    TestConfig().a.isNaN should be (true)
  }


  test("set a string") {
    case class TestConfig() extends ConfigMap {
      def a = get[String]("a") getOrElse ""
    }

    def update(config: ConfigMap) = config.set("a" -> "foo")

    val c = TestConfig() 
    update(c)
    c.a should be ("foo")
  }

  test("set an int") {
    case class TestConfig() extends ConfigMap {
      def a = get[Int]("a") getOrElse -1
    }

    def update(config: ConfigMap) = config.set("a" -> "42")

    val c = TestConfig() 
    update(c)
    c.a should be (42)
  }

  test("set an double") {
    case class TestConfig() extends ConfigMap {
      def a = get[Double]("a") getOrElse Double.NaN
    }

    def update(config: ConfigMap) = config.set("a" -> "23.23")

    val c = TestConfig() 
    update(c)
    c.a should be (23.23)
  }

}
