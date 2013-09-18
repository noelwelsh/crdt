package crdt

import org.specs2.mutable._
import org.specs2.ScalaCheck
import org.scalacheck._
import org.scalacheck.Prop.forAll

import com.twitter.algebird.Semigroup
import com.twitter.algebird.Operators._
import scala.math.Ordering

class PCounterSpec extends Specification with ScalaCheck {

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


  def associativeLaw[PCounter[_,_], Id, A](implicit
    pcounter: Arbitrary[PCounter[Id, A]],
    pcounterSemigroup: Semigroup[PCounter[Id, A]],
    semigroup: Semigroup[A],
    ordering: Ordering[A]
  ) =
    forAll { (p1: PCounter[Id, A], p2: PCounter[Id, A], p3: PCounter[Id, A]) =>
      (p1 + p2) + p3 == p1 + (p2 + p3)
    }

  def commutativeLaw[PCounter[_,_], Id, A](implicit
    pcounter: Arbitrary[PCounter[Id, A]],
    pcounterSemigroup: Semigroup[PCounter[Id, A]],
    semigroup: Semigroup[A],
    ordering: Ordering[A]
  ) =
    forAll { (p1: PCounter[Id, A], p2: PCounter[Id, A]) =>
      (p1 + p2) == (p2 + p1)
    }


  "PCounter" should {

    "be associative and commutative" in {
      val stringIntTupleGen = for {
        string <- Gen.alphaStr
        int    <- Gen.chooseNum(0, 65536)
      } yield (string, int)

      implicit val pcounterGen = Arbitrary {
        for {
          alist <- Gen.containerOf[List, (String, Int)](stringIntTupleGen)
        } yield PCounter(Map(alist : _*))
      }

      associativeLaw && commutativeLaw
    }

  }
}
