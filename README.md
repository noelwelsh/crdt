crdt
====

Convergent and Commutative Replicated Data Types for Scala

[CRDTs](http://pagesperso-systeme.lip6.fr/Marc.Shapiro/papers/Comprehensive-CRDTs-RR7506-2011-01.pdf) give you eventually consistent data structure for free. Merge copies of your data without fear!

This library is written in Scala 2.10 and uses type classes from [Algebird](https://github.com/twitter/algebird).

# PCounter

A `PCounter` is an increment only counter. It is defined for any type `A` for which there is both a `Semigroup[A]` and a `Ordering[A]`. A `PCounter` associates counts with `Id`s, which typically refer to different machines. You may increment the counter given an `Id`

```scala
import com.twitter.algebird.Semigroup
import scala.math.Ordering
import crdt.PCounter

// Create an empty counter
// Machines are identified by Strings. Counters are Ints
val counter = PCounter[String, Int]()

// Add some values
val updated = counter + ("a", 1) + ("b", 2) + ("c", 3)

// Get the total
updated.get // 1 + 2 + 3 = 6

// Increment the value associated with a machine
updated + ("a", 2)  // the total is now 3 + 2 + 3 = 8
```

As a `PCounter` is a `Monoid` you can merge `PCounter`s together. Merging keeps the largest element associated with each machine.

```scala
import com.twitter.algebird.Operators._

val counter1 = PCounter[String, Int]() + ("a", 1)
val counter2 = PCounter[String, Int]() + ("a", 2)

val counter3 = counter1 + counter2
counter3.get // 2
```

If for some reason you want to see the elements associated with machines, `values` returns a `Map[Id, A]` from a `PCounter[Id, A]`:

```scala
val counter1 = PCounter[String, Int]() + ("a", 1)
counter1.values // Map[String,Int] = Map(a -> 1)
```
