trait PNCounter[Id,Elt] {
  def inc(id: Id, amt: Elt)(implicit m: Monoid[Elt])
  def dec(id: Id, amt: Elt)(implicit m: Monoid[Elt])
  def total(implicit m: Group[Elt]): Elt
  def merge(c: GCounter[Id, Elt])(implicit m: Group[Elt @@ Max]): GCounter[Id, Elt]
}
