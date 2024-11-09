package domain

import scala.annotation.targetName
import scala.util.{Failure, Success, Try}

class ParserError(message: String) extends Exception(message)

sealed trait Parser[T] {
  def parse(s: String): Try[(T, String)]

  @targetName("or")
  def <|>[U](right: Parser[U]): Parser[T | U] = {
    val left = this
    new Parser[T | U] {
      def parse(s: String): Try[(T | U, String)] = {
        left.parse(s).recoverWith(_ => right.parse(s))
      }
    }
  }

  @targetName("concat")
  def <>[U](right: Parser[U]): Parser[(T, U)] = {
    val left = this
    new Parser[(T, U)] {
      def parse(s: String): Try[((T, U), String)] = {
        for {
          (v1, r1) <- left.parse(s)
          (v2, r2) <- right.parse(r1)
        } yield ((v1, v2), r2)
      }
    }
  }

  @targetName("rightmost")
  def ~>[U](right: Parser[U]): Parser[U] = {
    val left = this
    new Parser[U] {
      def parse(s: String): Try[(U, String)] = {
        for {
          (_, r1) <- left.parse(s)
          (v2, r2) <- right.parse(r1)
        } yield (v2, r2)
      }
    }
  }

  @targetName("leftmost")
  def <~[U](right: Parser[U]): Parser[T] = {
    val left = this
    new Parser[T] {
      def parse(s: String): Try[(T, String)] = {
        for {
          (v1, r1) <- left.parse(s)
          (v2, r2) <- right.parse(r1)
        } yield (v1, r2)
      }
    }
  }

  def sepBy(sep: Parser[_]): Parser[List[T]] = {
    val contenido = this
    new Parser[List[T]] {
      def parse(s: String): Try[(List[T], String)] = {
        contenido.parse(s) // Llamada inicial
          .flatMap((v1, r1) =>
            sep.parse(r1).map(_._2) // Intentar parsear el separador y ignorar el resultado
              .flatMap(this.parse) // Llamada recursiva, solo si el separador es exitoso
              .fold(
                _ => Success(List(v1), r1), // Si es un fallo, solo devolver el valor parseado
                (vs, r2) => Success(v1 :: vs, r2) // Si no es un fallo concatenar los resultados
              )
          )
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