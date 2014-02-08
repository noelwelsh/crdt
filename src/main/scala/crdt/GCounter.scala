package crdt

import com.twitter.algebird.{Max, Monoid, Semigroup}
import com.twitter.algebird.Operators._

/**
  * A GCounter is a CRDT counter that allows only increments.
  *
  * A GCounter defined for any type A for which a Monoid[A] and
  * Monoid[Max[A]] exists, and it is also itself a Monoid.
  */
case class GCounter[Id, A](val values: Map[Id, A] = Map.empty[Id, A]) {

  /** "Increment" the element */
  def +(id: Id, newValue: A)(implicit semigroup: Semigroup[A]): GCounter[Id, A] =
    GCounter(Map(id -> newValue) + values)

  /** Get the current value of the counter */
  def count(implicit m: Monoid[A]): A =
    values.foldLeft(m.zero)((accum, elt) =>
      accum + elt._2
    )

  def merge(c: GCounter[Id, A])(implicit m: Monoid[Max[A]]): GCounter[Id, A] = {
    // Modified from MapMonoid to use largest element
    //
    // Scala maps can reuse internal structure, so don't copy just
    // add into the bigger one: This really saves computation when
    // adding lots of small maps into big ones (common)
    val x = this.values
    val y = c.values
    val (big, small, bigOnLeft) = if(x.size > y.size) { (x,y,true) } else { (y,x,false) }
    GCounter(
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
  }

}

object GCounter {

  implicit def monoid[Id, A](implicit m: Monoid[Max[A]]): Monoid[GCounter[Id, A]] =
    Monoid.from( GCounter[Id,A]() )( (l, r) => { l.merge(r) } )

}
