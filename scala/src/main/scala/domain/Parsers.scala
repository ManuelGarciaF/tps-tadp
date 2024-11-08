package domain

import scala.util.{Failure, Success, Try}

class ParserError(message: String) extends Exception(message)

sealed trait Parser[T] {
  def parse(s: String): Try[(T, String)]

  def <|>[U](right: Parser[U]): Parser[Either[T, U]] = {
    val left = this
    new Parser[Either[T, U]] {
      def parse(s: String): Try[(Either[T, U], String)] = {
        left.parse(s) match {
          case Success((value, rest)) => Success((Left(value), rest))
          case Failure(_) => right.parse(s) match {
            case Success((value, rest)) => Success((Right(value), rest))
            case Failure(e) => Failure(e)
          }
        }
      }
    }
  }

  def <>[U](right: Parser[U]): Parser[(T, U)] = {
    val left = this
    new Parser[(T, U)] {
      def parse(s: String): Try[((T, U), String)] = {
        left.parse(s).flatMap(t => right.parse(t._2) match {
          case Success((u, rest)) => Success(((t._1, u), rest))
          case Failure(e) => Failure(e)
        })
      }
    }
  }
}

case object anyChar extends Parser[Char] {
  def parse(s: String): Try[(Char, String)] = s match {
    case "" => Failure(new ParserError("No hay más caracteres"))
    case s => Success((s.head, s.tail))
  }
}

case class char(c: Char) extends Parser[Char] {
  def parse(s: String): Try[(Char, String)] = s match {
    case "" => Failure(new ParserError("No hay más caracteres"))
    case s if s.head == c => Success((c, s.tail))
    case other => Failure(new ParserError(s"Se esperaba el caracter $c pero se encontró ${other.head}"))
  }
}

case object digit extends Parser[Char] {
  def parse(s: String): Try[(Char, String)] = s match {
    case "" => Failure(new ParserError("No hay más caracteres"))
    case s if s.head.isDigit => Success((s.head, s.tail))
    case other => Failure(new ParserError(s"Se esperaba un dígito pero se encontró ${other.head}"))
  }
}

case class string(expected: String) extends Parser[String] {
  def parse(s: String): Try[(String, String)] = s match {
    case s if s.startsWith(expected) => Success((expected, s.drop(expected.length)))
    case other => Failure(new ParserError(s"Se esperaba la cadena $expected pero se encontró $other"))
  }
}

case object number extends Parser[Int] {
  def parse(s: String): Try[(Int, String)] = s match {
    case "" => Failure(new ParserError("No hay más caracteres"))
    case s if s.head.isDigit => Success((s.takeWhile(_.isDigit).toInt, s.dropWhile(_.isDigit)))
    case other => Failure(new ParserError(s"Se esperaba un dígito pero se encontró ${other.head}"))
  }
}

case object integer extends Parser[Int] {
  def parse(s: String): Try[(Int, String)] = s match {
    case "" => Failure(new ParserError("No hay más caracteres"))
    case s if s.head.isDigit => number.parse(s)
    case s if s.head == '-' => number.parse(s.tail) match {
      case Success((n, rest)) => Success((-n, rest))
      case Failure(e) => Failure(e)
    }
    case other => Failure(new ParserError(s"Se esperaba un dígito pero se encontró ${other.head}"))
  }
}

case object double extends Parser[Double] {
  def parse(s: String): Try[(Double, String)] = s match {
    case "" => Failure(new ParserError("No hay más caracteres"))
    case s => integer.parse(s) match {
      case Success((start, rest)) => if (rest.headOption.contains('.')) {
        number.parse(rest.tail) match {
          case Success((end, rest2)) => Success((s"$start.$end".toDouble, rest2))
          case Failure(_) => Failure(new ParserError(s"Se esperaba un número entero pero se encontró ${rest.head}"))
        }
      } else {
        Success((start, rest))
      }
      case Failure(_) => Failure(new ParserError(s"Se esperaba un número entero pero se encontró ${s.head}"))
    }
  }
}
