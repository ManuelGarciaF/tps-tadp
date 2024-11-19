import domain.*
import org.scalatest.freespec.AnyFreeSpec

class OperationsTest extends AnyFreeSpec {
  "satisfies" - {
    "deberia funcionar si la condicion se cumple" in {
      assert(integer.satisfies(_ > 0)("1abc").get == (1, "abc"))
    }

    "deberia fallar si la condicion no se cumple" in {
      assert(integer.satisfies(_ > 0)("-2abc").isFailure)
    }

    "deberia fallar si el parser falla" in {
      assert(integer.satisfies(_ > 0)("abc").isFailure)
    }
  }

  "opt" - {
    "Deberia devolver un valor si el parser funciona" in {
      assert(integer.opt("123abc").get == (Some(123), "abc"))
    }

    "Deberia devolver None si el parser falla" in {
      assert(integer.opt("abc").get == (None, "abc"))
    }

    "Deberia funcionar con concatenaciones" in {
      val talVezIn = string("in").opt
      val precedencia = talVezIn <> string("fija")
      assert(precedencia("infija").get == ((Some("in"), "fija"), ""))
      assert(precedencia("fija").get == ((None, "fija"), ""))
    }
  }

  "kleene (*)" - {
    "Deberia devolver una lista vacia si el parser falla" in {
      assert(char('a').*.apply("bc").get == (List(), "bc"))
    }

    "Deberia devolver una lista con los valores parseados" in {
      assert(char('a').*.apply("aaabc").get == (List('a', 'a', 'a'), "bc"))
    }

    "Deberia cortar al primer fallo" in {
      assert(char('a').*.apply("aa1aaabc").get == (List('a', 'a'), "1aaabc"))
    }
  }

  "positiveClause (+)" - {
    "Deberia devolver una lista con los valores parseados" in {
      assert(char('a').+.apply("aaabc").get == (List('a', 'a', 'a'), "bc"))
    }

    "Deberia fallar si no hay ningun elemento" in {
      assert(char('a').+.apply("bc").isFailure)
    }
  }

  "map" - {
    "Deberia aplicar la funcion al valor parseado" in {
      assert(char('a').map(_.toInt).apply("abc").get == (97, "bc"))
    }

    "Deberia fallar si el parser falla" in {
      assert(char('a').map(_.toInt).apply("bc").isFailure)
    }

    "Deberia cumplir el ejemplo dado" in {
      case class Persona(nombre: String, apellido: String)
      val alphaNum = anyChar.satisfies(_.isLetterOrDigit)
      val personaParser = (alphaNum.* <> (char(' ') ~> alphaNum.*))
        .map { case (nombre, apellido) => Persona(nombre.toString(), apellido.toString()) }
    }
  }
}
