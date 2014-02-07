package crdt

import com.twitter.algebird.{Max, Monoid, Group}
import com.twitter.algebird.Operators._

trait PNCounter[Id,Elt] {
  def inc(id: Id, amt: Elt)(implicit m: Monoid[Elt])
  def dec(id: Id, amt: Elt)(implicit m: Monoid[Elt])
  def total(implicit m: Group[Elt]): Elt
  def merge(c: PNCounter[Id, Elt])(implicit m: Group[Max[Elt]]): PNCounter[Id, Elt]
}
