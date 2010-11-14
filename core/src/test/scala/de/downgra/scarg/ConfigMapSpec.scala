package de.downgra.scarg

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class ConfigMapSpec extends FunSuite with ShouldMatchers {

  test("default value string") {
    class TestConfig extends ConfigMap(Map()) {
      val a = get[String]("a") getOrElse ""
    }
    (new TestConfig).a should be ("")
  }

  test("default value int") {
    class TestConfig extends ConfigMap(Map()) {
      val a = get[Int]("a") getOrElse -1
    }
    (new TestConfig).a should be (-1)
  }

  test("default value double") {
    class TestConfig extends ConfigMap(Map()) {
      val a = get[Double]("a") getOrElse Double.NaN
      val b = get[Double]("b", 23.42)
    }
    (new TestConfig).a.isNaN should be (true)
    (new TestConfig).b should be (23.42)
  }

  test("default value boolean") {
    class TestConfig extends ConfigMap(Map()) {
      val a = get[Boolean]("a") getOrElse false
      val b = get[Boolean]("b", true)
    }
    (new TestConfig).a should be (false)
    (new TestConfig).b should be (true)
  }

  test("default list value") {
    class TestConfig extends ConfigMap(Map()) {
      val a = getList[Int]("a")
      val b = getList[Int]("b", Nil)
    }
    (new TestConfig).a should be (Nil)
    (new TestConfig).b should be (Nil)
  }

  test("int list value") {
    class TestConfig extends ConfigMap(Map("a" -> List("23", "42"))) {
      val a = getList[Int]("a")
    }
    (new TestConfig).a should be (List(23, 42))
  }

  test("boolean list value") {
    class TestConfig extends ConfigMap(Map("a" -> List("true", "FALSE", "0", "yEs"))) {
      val a = getList[Boolean]("a")
    }
    (new TestConfig).a should be (List(true, false, false, true))
  }

  test("get default option value") {
    class TestConfig extends ConfigMap(Map()) {
      val a = get[Int]("a")
    }
    (new TestConfig).a should be (None)
  }

  test("get option value") {
    class TestConfig extends ConfigMap(Map("a" -> List("42"))) {
      val a = get[Int]("a")
    }
    (new TestConfig).a should be (Some(42))
  }

  test("invalid option value") {
    class TestConfig extends ConfigMap(Map("a" -> List("foo"))) {
      val a = get[Int]("a")
    }
    evaluating { 
      new TestConfig
    } should produce [IllegalArgumentException]
  }

  test("alternate syntax") {
    class TestConfig extends ConfigMap(Map("a" -> List("42"),
                                           "b" -> List("23.42", "42.23"))) {
      val a = ("a").as[Int]
      val b = ("b").asList[Double]
    }
    (new TestConfig).a should be (Some(42))
    (new TestConfig).b should be (List(23.42, 42.23))
  }
}
