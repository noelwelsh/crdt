package crdt

import org.specs2.mutable._

import com.twitter.algebird.Semigroup
import com.twitter.algebird.Operators._
import scala.math.Ordering

class PCounterSpec extends Specification {

  "PCounter.get" should {

    "sum all values across all id using given Semigroup" in {
      implicit val multiplicationSemigroup: Semigroup[Int] =
        Semigroup.from((l, r) => l * r)

      val counter = PCounter[String, Int]()

      (counter + ("a", 1) + ("b", 2) + ("c", 3)).get mustEqual Some(3 * 2 * 1)
    }

  }

  "PCounter.+" should {

    "increment the value at the given id" in {
      val counter = PCounter[String, Int]()

      (counter + ("a", 1) + ("a", 2)).get mustEqual Some(1 + 2)
    }

  }

  "PCounter Semigroup" should {

    "keep max elements for each id" in {
      val counter =
        PCounter(Map("a" -> 1, "b" -> 2, "c" -> 3)) + PCounter(Map("b" -> 3, "c" -> 2))

      counter.get mustEqual Some(1 + 3 + 3)
      counter.values("a") mustEqual 1
      counter.values("b") mustEqual 3
      counter.values("c") mustEqual 3
    }

  }

}
