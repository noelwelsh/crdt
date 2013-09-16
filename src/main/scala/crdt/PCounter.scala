package crdt

import com.twitter.algebird.{Max, Semigroup}
import com.twitter.algebird.Operators._
import scala.math.Ordering

/**
  * A PCounter is a CRDT counter that allows only increments.
  *
  * A PCounter defined for any type A for which a Semigroup[A] and Ordering[A] exists,
  * and it is also itself a Semigroup. A PCounter is also commutative.
  *
  */
case class PCounter[Id, A](val values: Map[Id, A] = Map.empty[Id, A]) {

  /** "Increment" the element */
  def +(id: Id, newValue: A)(implicit semigroup: Semigroup[A]): PCounter[Id, A] =
    PCounter(Map(id -> newValue) + values)

  /** Get the current value of the element */
  def get(implicit semigroup: Semigroup[A]): Option[A] =
    values.foldLeft(None: Option[A])((accum, elt) =>
      accum match {
        case None    => Some(elt._2)
        case Some(x) => Some(x + elt._2)
      }
    )

}

object PCounter {

  implicit def semigroup[Id, A](implicit semigroup: Semigroup[A], ordering: Ordering[A]): Semigroup[PCounter[Id, A]] =
    Semigroup.from((l, r) => {
      // Modified from MapMonoid to use largest element
      //
      // Scala maps can reuse internal structure, so don't copy just
      // add into the bigger one: This really saves computation when
      // adding lots of small maps into big ones (common)
      val x = l.values
      val y = r.values
      val (big, small, bigOnLeft) = if(x.size > y.size) { (x,y,true) } else { (y,x,false) }
      PCounter(
        small.foldLeft(big) { (oldMap, kv) =>
          val newV = big
            .get(kv._1)
            .map { bigV =>
              if(bigOnLeft)
                (Max(bigV) + Max(kv._2)).get
              else
                (Max(kv._2) + Max(bigV)).get
            }
            .getOrElse(kv._2)

          oldMap + (kv._1 -> newV)
        }
      )
    })

}
