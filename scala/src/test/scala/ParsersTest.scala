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

    "deberia fallar si no hay una sucesion de digitos despues del punto" in {
      assert(double("123.abc").isFailure)
    }

    "debería fallar si el primer caracter no es un dígito" in {
      assert(double("abc").isFailure)
    }
  }

  "or (<|>)" - {
    "deberia devolver el primer parser si tiene exito" in {
      assert((char('a') <|> integer)("abc").get == ('a', "bc"))
    }

    "deberia devolver el segundo parser si tiene exito" in {
      assert((char('a') <|> integer)("123abc").get == (123, "abc"))
    }

    "deberia fallar si ambos parsers fallan" in {
      assert((char('a') <|> integer)("bc").isFailure)
    }

    "deberia darle prioridad al primer parser" in {
      assert((integer <|> char('1'))("1bc").get == (1, "bc"))
    }
  }

  "concat (<>)" - {
    "deberia concatenar dos parsers" in {
      assert((char('a') <> char('b'))("abc").get == (('a', 'b'), "c"))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') <> char('b'))("bc").isFailure)
    }

    "deberia fallar si el segundo parser falla" in {
      assert((char('a') <> char('b'))("ac").isFailure)
    }
  }

  "rightmost (~>)" - {
    "deberia devolver el resultado del segundo" in {
      assert((char('a') ~> char('b'))("abc").get == ('b', "c"))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') ~> char('b'))("bc").isFailure)
    }

    "deberia fallar si el segundo parser falla" in {
      assert((char('a') ~> char('b'))("ac").isFailure)
    }
  }

  "leftmost (<~)" - {
    "deberia devolver el resultado del segundo" in {
      assert((char('a') <~ char('b'))("abc").get == ('a', "c"))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') <~ char('b'))("bc").isFailure)
    }

    "deberia fallar si el segundo parser falla" in {
      assert((char('a') <~ char('b'))("ac").isFailure)
    }
  }

  "sepBy" - {
    "deberia devolver una lista de elementos" in {
      assert((integer sepBy char(','))("123,456,789").get == (List(123, 456, 789), ""))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') sepBy char(','))("b,a,a").isFailure)
    }

    "deberia dar el primer valor si el segundo parser falla" in {
      assert((char('a') sepBy char(','))("a.a,b").get == (List('a'), ".a,b"))
    }
  }
}
