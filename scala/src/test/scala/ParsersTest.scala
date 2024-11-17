import domain.*
import org.scalatest.freespec.AnyFreeSpec

class ParsersTest extends AnyFreeSpec {

  "anyChar" - {
    "debería fallar si no hay más caracteres" in {
      assert(anyChar("").isFailure)
    }

    "debería devolver el primer caracter y el resto de la cadena" in {
      assert(anyChar("abc").get == ('a', "bc"))
    }
  }

  "char" - {
    "debería fallar si no hay más caracteres" in {
      assert(char('a')("").isFailure)
    }

    "debería fallar si el primer caracter no es el esperado" in {
      assert(char('a')("b").isFailure)
    }

    "debería devolver el primer caracter y el resto de la cadena si el primer caracter es el esperado" in {
      assert(char('a')("abc").get == ('a', "bc"))
    }
  }

  "digit" - {
    "debería fallar si no hay más caracteres" in {
      assert(digit("").isFailure)
    }

    "debería fallar si el primer caracter no es un dígito" in {
      assert(digit("a").isFailure)
    }

    "debería devolver el primer caracter y el resto de la cadena si el primer caracter es un dígito" in {
      assert(digit("1abc").get == ('1', "abc"))
    }
  }

  "string" - {
    "debería fallar si la cadena esperada no está al principio" in {
      assert(string("abc")("bcasdfs").isFailure)
    }

    "debería devolver la cadena esperada y el resto de la cadena si la cadena esperada está al principio" in {
      assert(string("abc")("abcde").get == ("abc", "de"))
    }
  }

  "integer" - {
    "debería fallar si no hay más caracteres" in {
      assert(integer("").isFailure)
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito" in {
      assert(integer("123abc").get == (123, "abc"))
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito y es negativo" in {
      assert(integer("-123abc").get == (-123, "abc"))
    }

    "deberia solo devolver el primer numero entero" in {
      assert(integer("123-456").get == (123, "-456"))
    }

    "deberia no parsear un guion solo" in {
      assert(integer("-sabasdf").isFailure)
    }
  }

  "double" - {
    "debería fallar si no hay más caracteres" in {
      assert(double("").isFailure)
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito" in {
      assert(double("123abc").get == (123, "abc"))
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito y es negativo" in {
      assert(double("-123abc").get == (-123, "abc"))
    }

    "debería devolver el número decimal y el resto de la cadena si el primer caracter es un dígito y tiene parte decimal" in {
      assert(double("123.456abc").get == (123.456, "abc"))
    }

    "debería devolver el número decimal y el resto de la cadena si el primer caracter es un dígito y tiene parte decimal y es negativo" in {
      assert(double("-123.456abc").get == (-123.456, "abc"))
    }

//    "deberia fallar si no hay una sucesion de digitos despues del punto" in {
//      assert(double("123.abc").isFailure)
//    }

    "debería fallar si el primer caracter no es un dígito" in {
      assert(double("abc").isFailure)
    }
  }

}
