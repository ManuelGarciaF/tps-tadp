import domain.*
import org.scalatest.freespec.AnyFreeSpec

def noWhitespace(s: String): String = s.filter(!_.isWhitespace)

class ImageParserTest extends AnyFreeSpec {
  "point" - {
    "Deberia poder parsear un punto" in {
      assert(point("1@2abc").get == ((1, 2), "abc"))
    }

    "Deberia fallar si no hay un @ en el medio" in {
      assert(point("1.2").isFailure)
    }
  }

  "triangle" - {
    "Deberia poder parsear un triangulo" in {
      assert(triangle(noWhitespace("triangulo[1 @ 2, 3 @ 4, 5 @ 6]abcabc")).get == (Triangle((1, 2), (3, 4), (5, 6)), "abcabc"))
    }

    "Deberia fallar si no suficientes puntos" in {
      assert(triangle(noWhitespace("triangulo[1 @ 2, 3 @ 4]")).isFailure)
    }

    "Deberia fallar si un elemento no es un punto" in {
      assert(triangle(noWhitespace("triangulo[1 @ 2, 3 @ 4, 5]")).isFailure)
    }

    "Deberia fallar si faltan comas" in {
      assert(triangle(noWhitespace("triangulo[1 @ 2 3 @ 4 5 @ 6]")).isFailure)
    }

    "Deberia fallar si no esta cerrado" in {
      assert(triangle(noWhitespace("triangulo[1 @ 2, 3 @ 4, 5 @ 6")).isFailure)
    }
  }

  "rectangle" - {
    "Deberia poder parsear un rectangulo" in {
      assert(rectangle(noWhitespace("rectangulo[1 @ 2, 3 @ 4]abcabc")).get == (Rectangle((1, 2), (3, 4)), "abcabc"))
    }

    "Deberia fallar si no hay dos puntos" in {
      assert(rectangle(noWhitespace("rectangulo[1 @ 2]")).isFailure)
    }

    "Deberia fallar si un elemento no es un punto" in {
      assert(rectangle(noWhitespace("rectangulo[1 @ 2, 3]")).isFailure)
    }

    "Deberia fallar si no hay comas" in {
      assert(rectangle(noWhitespace("rectangulo[1 @ 2 3 @ 4]")).isFailure)
    }

    "Deberia fallar si no esta cerrado" in {
      assert(rectangle(noWhitespace("rectangulo[1 @ 2, 3 @ 4")).isFailure)
    }
  }

  "circle" - {
    "Deberia poder parsear un circulo" in {
      assert(circle(noWhitespace("circulo[1 @ 2, 3]abcabc")).get == (Circle((1, 2), 3), "abcabc"))
    }

    "Deberia fallar si no hay un punto y un entero" in {
      assert(circle(noWhitespace("circulo[1 @ 2]")).isFailure)
    }

    "Deberia fallar si el radio no es un entero" in {
      assert(circle(noWhitespace("circulo[1 @ 2, a]")).isFailure)
    }

    "Deberia fallar si no hay comas" in {
      assert(circle(noWhitespace("circulo[1 @ 2 3]")).isFailure)
    }

    "Deberia fallar si no esta cerrado" in {
      assert(circle(noWhitespace("circulo[1 @ 2, 3")).isFailure)
    }
  }

  "group" - {
    "Deberia poder parsear un grupo" in {
      val str = noWhitespace(
        """grupo(
          |  triangulo[200 @ 50, 101 @ 335, 299 @ 335],
          |  circulo[200 @ 350, 100]
          |)""".stripMargin)
      assert(group(str).get ==
        (Group(List(Triangle((200, 50), (101, 335), (299, 335)), Circle((200, 350), 100))), "")
      )
    }

    "Deberia soportar grupos anidados" in {
      val str = noWhitespace(
        """grupo(
          |  grupo(
          |    triangulo[250 @ 150, 150 @ 300, 350 @ 300]
          |  ),
          |  circulo[50 @ 100, 1]
          |)
          |""".stripMargin)

      assert(group(str).get ==
        (Group(List(Group(List(Triangle((250, 150), (150, 300), (350, 300)))), Circle((50, 100), 1))), "")
      )
    }

    "Deberia poder parsear el ejemplo" in {
      val str = noWhitespace(
        """grupo(
          |    grupo(
          |   	 triangulo[250 @ 150, 150 @ 300, 350 @ 300],
          |   	 triangulo[150 @ 300, 50 @ 450, 250 @ 450],
          |   	 triangulo[350 @ 300, 250 @ 450, 450 @ 450]
          |    ),
          |    grupo(
          |   	 rectangulo[460 @ 90, 470 @ 100],
          |   	 rectangulo[430 @ 210, 500 @ 220],
          |   	 rectangulo[430 @ 210, 440 @ 230],
          |   	 rectangulo[490 @ 210, 500 @ 230],
          |   	 rectangulo[450 @ 100, 480 @ 260]
          |    )
          |)
          |""".stripMargin)

      assert(group(str).get == (
        Group(List(
          Group(List(
            Triangle((250, 150), (150, 300), (350, 300)),
            Triangle((150, 300), (50, 450), (250, 450)),
            Triangle((350, 300), (250, 450), (450, 450))
          )),
          Group(List(
            Rectangle((460, 90), (470, 100)),
            Rectangle((430, 210), (500, 220)),
            Rectangle((430, 210), (440, 230)),
            Rectangle((490, 210), (500, 230)),
            Rectangle((450, 100), (480, 260))
          ))
        )),
      ""))
    }
  }

}
