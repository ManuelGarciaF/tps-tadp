import domain.*
import org.scalatest.freespec.AnyFreeSpec

class CombinatorsTest extends AnyFreeSpec {
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
