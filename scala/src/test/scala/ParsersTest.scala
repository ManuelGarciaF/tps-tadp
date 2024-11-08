import domain.*
import org.scalatest.freespec.AnyFreeSpec

class ParsersTest extends AnyFreeSpec {

  "anyChar" - {
    "debería fallar si no hay más caracteres" in {
      assert(anyChar.parse("").isFailure)
    }

    "debería devolver el primer caracter y el resto de la cadena" in {
      assert(anyChar.parse("abc").get == ('a', "bc"))
    }
  }

  "char" - {
    "debería fallar si no hay más caracteres" in {
      assert(char('a').parse("").isFailure)
    }

    "debería fallar si el primer caracter no es el esperado" in {
      assert(char('a').parse("b").isFailure)
    }

    "debería devolver el primer caracter y el resto de la cadena si el primer caracter es el esperado" in {
      assert(char('a').parse("abc").get == ('a', "bc"))
    }
  }

  "digit" - {
    "debería fallar si no hay más caracteres" in {
      assert(digit.parse("").isFailure)
    }

    "debería fallar si el primer caracter no es un dígito" in {
      assert(digit.parse("a").isFailure)
    }

    "debería devolver el primer caracter y el resto de la cadena si el primer caracter es un dígito" in {
      assert(digit.parse("1abc").get == ('1', "abc"))
    }
  }

  "string" - {
    "debería fallar si la cadena esperada no está al principio" in {
      assert(string("abc").parse("bcasdfs").isFailure)
    }

    "debería devolver la cadena esperada y el resto de la cadena si la cadena esperada está al principio" in {
      assert(string("abc").parse("abcde").get == ("abc", "de"))
    }
  }

  "integer" - {
    "debería fallar si no hay más caracteres" in {
      assert(integer.parse("").isFailure)
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito" in {
      assert(integer.parse("123abc").get == (123, "abc"))
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito y es negativo" in {
      assert(integer.parse("-123abc").get == (-123, "abc"))
    }

    "deberia solo devolver el primer numero entero" in {
      assert(integer.parse("123-456").get == (123, "-456"))
    }

    "deberia no parsear un guion solo" in {
      assert(integer.parse("-sabasdf").isFailure)
    }
  }

  "double" - {
    "debería fallar si no hay más caracteres" in {
      assert(double.parse("").isFailure)
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito" in {
      assert(double.parse("123abc").get == (123, "abc"))
    }

    "debería devolver el número entero y el resto de la cadena si el primer caracter es un dígito y es negativo" in {
      assert(double.parse("-123abc").get == (-123, "abc"))
    }

    "debería devolver el número decimal y el resto de la cadena si el primer caracter es un dígito y tiene parte decimal" in {
      assert(double.parse("123.456abc").get == (123.456, "abc"))
    }

    "debería devolver el número decimal y el resto de la cadena si el primer caracter es un dígito y tiene parte decimal y es negativo" in {
      assert(double.parse("-123.456abc").get == (-123.456, "abc"))
    }

    "deberia fallar si no hay una sucesion de digitos despues del punto" in {
      assert(double.parse("123.abc").isFailure)
    }

    "debería fallar si el primer caracter no es un dígito" in {
      assert(double.parse("abc").isFailure)
    }
  }

  "or (<|>)" - {
    "deberia devolver el primer parser si tiene exito" in {
      assert((char('a') <|> integer).parse("abc").get == ('a', "bc"))
    }

    "deberia devolver el segundo parser si tiene exito" in {
      assert((char('a') <|> integer).parse("123abc").get == (123, "abc"))
    }

    "deberia fallar si ambos parsers fallan" in {
      assert((char('a') <|> integer).parse("bc").isFailure)
    }

    "deberia darle prioridad al primer parser" in {
      assert((integer <|> char('1')).parse("1bc").get == (1, "bc"))
    }
  }

  "concat (<>)" - {
    "deberia concatenar dos parsers" in {
      assert((char('a') <> char('b')).parse("abc").get == (('a', 'b'), "c"))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') <> char('b')).parse("bc").isFailure)
    }

    "deberia fallar si el segundo parser falla" in {
      assert((char('a') <> char('b')).parse("ac").isFailure)
    }
  }

  "rightmost (~>)" - {
    "deberia devolver el resultado del segundo" in {
      assert((char('a') ~> char('b')).parse("abc").get == ('b', "c"))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') ~> char('b')).parse("bc").isFailure)
    }

    "deberia fallar si el segundo parser falla" in {
      assert((char('a') ~> char('b')).parse("ac").isFailure)
    }
  }

  "leftmost (<~)" - {
    "deberia devolver el resultado del segundo" in {
      assert((char('a') <~ char('b')).parse("abc").get == ('a', "c"))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') <~ char('b')).parse("bc").isFailure)
    }

    "deberia fallar si el segundo parser falla" in {
      assert((char('a') <~ char('b')).parse("ac").isFailure)
    }
  }

  "sepBy" - {
    "deberia devolver una lista de elementos" in {
      assert((integer sepBy char(',')).parse("123,456,789").get == (List(123, 456, 789), ""))
    }

    "deberia fallar si el primer parser falla" in {
      assert((char('a') sepBy char(',')).parse("b,a,a").isFailure)
    }

    "deberia dar el primer valor si el segundo parser falla" in {
      assert((char('a') sepBy char(',')).parse("a.a,b").get == (List('a'), ".a,b"))
    }

    "deberia devolver una lista vacia si no hay elementos" in {
      assert((char('a') sepBy char(',')).parse("").get == (List(), ""))
    }
  }
}
