package domain

import scala.annotation.targetName
import scala.util.{Failure, Success, Try}

class NoMoreCharactersError extends Exception("No hay más caracteres")

class UnexpectedCharacterError extends Exception("Caracter inesperado")

sealed trait Parser[T] {
  def apply(s: String): Try[(T, String)]

  @targetName("or")
  def <|>[U](right: Parser[U]): Parser[T | U] = {
    val left = this
    new Parser[T | U] {
      def apply(s: String): Try[(T | U, String)] = {
        left(s).recoverWith(_ => right(s))
      }
    }
  }

  @targetName("concat")
  def <>[U](right: Parser[U]): Parser[(T, U)] = {
    val left = this
    new Parser[(T, U)] {
      def apply(s: String): Try[((T, U), String)] = {
        for {
          (v1, r1) <- left(s)
          (v2, r2) <- right(r1)
        } yield ((v1, v2), r2)
      }
    }
  }

  @targetName("rightmost")
  def ~>[U](right: Parser[U]): Parser[U] = {
    val left = this
    new Parser[U] {
      def apply(s: String): Try[(U, String)] = {
        for {
          (_, r1) <- left(s)
          (v2, r2) <- right(r1)
        } yield (v2, r2)
      }
    }
  }

  @targetName("leftmost")
  def <~[U](right: Parser[U]): Parser[T] = {
    val left = this
    new Parser[T] {
      def apply(s: String): Try[(T, String)] = {
        for {
          (v1, r1) <- left(s)
          (_, r2) <- right(r1)
        } yield (v1, r2)
      }
    }
  }

  def sepBy(sep: Parser[_]): Parser[List[T]] = (this <> (sep ~> this).*).map((v, vs) => v :: vs)

  def satisfies(f: T => Boolean): Parser[T] = {
    val orig = this
    new Parser[T] {
      def apply(s: String): Try[(T, String)] = orig(s).filter((v, _) => f(v))
    }
  }

  def opt: Parser[Option[T]] = {
    val orig = this
    new Parser[Option[T]] {
      def apply(s: String): Try[(Option[T], String)] = orig(s)
        .map((v, r) => (Some(v), r))
        .recover(_ => (None, s))
    }
  }

  @targetName("kleene")
  def * : Parser[List[T]] = {
    val orig = this
    new Parser[List[T]] {
      def apply(s: String): Try[(List[T], String)] = Try(applyTail(List(), s)) // La clausura de kleene nunca falla

      // Tail recursive
      def applyTail(vals: List[T], rest: String): (List[T], String) = orig(rest)
        .map((v, r) => applyTail(v :: vals, r))
        .recover(_ => (vals.reverse, rest)) // Hay que invertirla ya que se agrega al principio
        .get
    }
  }

  def map[U](f: T => U): Parser[U] = {
    val orig = this
    new Parser[U] {
      def apply(s: String): Try[(U, String)] = orig(s).map((v, r) => (f(v), r))
    }
  }

  @targetName("positiveClause") // Se lo puede definir en funcion de la clausura de kleene
  def + : Parser[List[T]] = (this <> this.*).map((v, vs) => v :: vs)
}

case object anyChar extends Parser[Char] {
  def apply(s: String): Try[(Char, String)] = s.headOption.fold
    (Failure(new NoMoreCharactersError))
    (h => Success((h, s.tail)))
}

case class string(expected: String) extends Parser[String] {
  def apply(s: String): Try[(String, String)] = s match {
    case s if s.startsWith(expected) => Success((expected, s.drop(expected.length)))
    case _ => Failure(new UnexpectedCharacterError)
  }
}

// Se pueden reimplementar casi todos los parsers a base de anychar, filter y map
val char: Char => Parser[Char] = c => anyChar.satisfies(_ == c)

val digit: Parser[Char] = anyChar.satisfies(_.isDigit)

val number: Parser[Int] = digit.*.map(_.mkString.toInt)

val integer = (char('-').opt <> number).map {
  case (None, n) => n
  case (Some(_), n) => -n
}

val double = (integer <> (char('.') <> number).opt).map {
  case (i, None) => i.toDouble
  case (i, Some((_, d))) => s"$i.$d".toDouble
}

//case class char(c: Char) extends Parser[Char] {
//  def apply(s: String): Try[(Char, String)] = s.headOption.fold
//    (Failure(new NoMoreCharactersError))
//    (h => if (h == c) Success((h, s.tail)) else Failure(new UnexpectedCharacterError))
//}
//
//case object digit extends Parser[Char] {
//  def apply(s: String): Try[(Char, String)] = s.headOption.fold
//    (Failure(new NoMoreCharactersError))
//    (h => if (h.isDigit) Success((s.head, s.tail)) else Failure(new UnexpectedCharacterError))
//}
//
//
//case object number extends Parser[Int] {
//  def apply(s: String): Try[(Int, String)] = s.headOption.fold
//    (Failure(new NoMoreCharactersError))
//    (h => if (h.isDigit) Success((s.takeWhile(_.isDigit).toInt, s.dropWhile(_.isDigit)))
//    else Failure(new UnexpectedCharacterError))
//}

//case object integer extends Parser[Int] {
//  def apply(s: String): Try[(Int, String)] = s.headOption.map {
//    case '-' => number(s.tail).map((n, rest) => (-n, rest))
//    case _ => number(s)
//  }.getOrElse(Failure(new NoMoreCharactersError))
//}

//case object double extends Parser[Double] {
//  def apply(s: String): Try[(Double, String)] = integer(s).flatMap((start, rest) =>
//    if (rest.headOption.contains('.')) {
//      number(rest.tail).map((end, rest2) => (s"$start.$end".toDouble, rest2))
//    } else {
//      Success((start.toDouble, rest))
//    }
//  )
//}