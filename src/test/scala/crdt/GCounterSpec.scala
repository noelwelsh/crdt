package crdt

import org.specs2.mutable._
import org.specs2.ScalaCheck
import org.scalacheck._
import org.scalacheck.Prop.forAll

import com.twitter.algebird.{Max, Monoid}
import com.twitter.algebird.Operators._

class GCounterSpec extends Specification with ScalaCheck {

  "GCounter.count" should {

    "sum all values across all id using given Monoid" in {
      implicit val multiplicationMonoid: Monoid[Int] =
        Monoid.from(1)((l, r) => l * r)

      val counter = GCounter[String, Int]()

      (counter + ("a", 1) + ("b", 2) + ("c", 3)).count mustEqual (3 * 2 * 1)
    }

  }

  "GCounter.+" should {

    "increment the value at the given id" in {
      val counter = GCounter[String, Int]()

      (counter + ("a", 1) + ("a", 2)).count mustEqual (1 + 2)
    }

  }

  "GCounter Monoid" should {

    "keep max elements for each id" in {
      val counter =
        GCounter(Map("a" -> 1, "b" -> 2, "c" -> 3)) + GCounter(Map("b" -> 3, "c" -> 2))

      counter.count mustEqual (1 + 3 + 3)
      counter.values("a") mustEqual 1
      counter.values("b") mustEqual 3
      counter.values("c") mustEqual 3
    }

  }


  def associativeLaw[GCounter[_,_], Id, A](implicit
    pcounter: Arbitrary[GCounter[Id, A]],
    pcounterMonoid: Monoid[GCounter[Id, A]],
    monoid: Monoid[A],
    ordering: Ordering[A]
  ) =
    forAll { (p1: GCounter[Id, A], p2: GCounter[Id, A], p3: GCounter[Id, A]) =>
      (p1 + p2) + p3 == p1 + (p2 + p3)
    }

  def commutativeLaw[GCounter[_,_], Id, A](implicit
    pcounter: Arbitrary[GCounter[Id, A]],
    pcounterMonoid: Monoid[GCounter[Id, A]],
    monoid: Monoid[A],
    ordering: Ordering[A]
  ) =
    forAll { (p1: GCounter[Id, A], p2: GCounter[Id, A]) =>
      (p1 + p2) == (p2 + p1)
    }


  "GCounter" should {

    "be associative and commutative" in {
      val stringIntTupleGen = for {
        string <- Gen.alphaStr
        int    <- Gen.chooseNum(0, 65536)
      } yield (string, int)

      implicit val pcounterGen = Arbitrary {
        for {
          alist <- Gen.containerOf[List, (String, Int)](stringIntTupleGen)
        } yield GCounter(Map(alist : _*))
      }

      associativeLaw && commutativeLaw
    }

  }
}
