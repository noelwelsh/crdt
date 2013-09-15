package crdt

import com.twitter.algebird.{Max, Semigroup}
import com.twitter.algebird.Operators._
import scala.math.Ordering

case class SemigroupCrdt[Id, A](val id: Id, val values: Map[Id, A] = Map.empty[Id, A])(implicit val semigroup: Semigroup[A]) {

  /** "Increment" the element */
  def +(newValue: A): SemigroupCrdt[Id, A] =
    SemigroupCrdt(id, Map(id -> newValue) + values)

  def value: Option[A] =
    values.foldLeft(None: Option[A])((accum, elt) =>
      accum match {
        case None    => Some(elt._2)
        case Some(x) => Some(x + elt._2)
      }
    )

}

object SemigroupCrdt {

  implicit def semigroup[Id, A](implicit ordering: Ordering[A]): Semigroup[SemigroupCrdt[Id, A]] =
    Semigroup.from((l, r) => {
      // Modified from MapMonoid to use largest element
      //
      // Scala maps can reuse internal structure, so don't copy just add into the bigger one:
      // This really saves computation when adding lots of small maps into big ones (common)
      implicit val semigroup = l.semigroup
      val x = l.values
      val y = r.values
      val (big, small, bigOnLeft) = if(x.size > y.size) { (x,y,true) } else { (y,x,false) }
      SemigroupCrdt(
        l.id,
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
