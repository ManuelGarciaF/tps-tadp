import domain.*
import org.scalatest.freespec.AnyFreeSpec

class ImageParserTest extends AnyFreeSpec {
  def noWhitespace(s: String): String = s.filter(!_.isWhitespace)

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

    "Deberia poder contener transformaciones" in {
      val str = noWhitespace(
        """grupo(
          |    color[60, 150, 200](
          |   	 triangulo[200 @ 50, 101 @ 335, 299 @ 335]
          |    ),
          |    escala[2.5, 1](
          |   	 circulo[200 @ 350, 100]
          |    ),
          |    rotacion[45](
          |   	 rectangulo[300 @ 0, 500 @ 200]
          |    ),
          |    traslacion[200, 50](
          |   	 triangulo[0 @ 100, 200 @ 300, 150 @ 500]
          |    )
          |)
          |""".stripMargin)

      assert(group(str).get == (
        Group(List(
          Color(
            Triangle((200, 50), (101, 335), (299, 335)),
            (60, 150, 200)
          ),
          Scale(
            Circle((200, 350), 100),
            (2.5, 1)
          ),
          Rotation(
            Rectangle((300, 0), (500, 200)),
            45
          ),
          Traslation(
            Triangle((0, 100), (200, 300), (150, 500)),
            (200, 50)
          )
        )),
        "")
      )
    }
  }

  "color" - {
    "Deberia funcionar con un triangulo" in {
      val str = noWhitespace(
        """color[60, 150, 200](
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335]
          |)mastexto""".stripMargin)

      assert(color(str).get == (
        Color(
          Triangle((200, 50), (101, 335), (299, 335)),
          (60, 150, 200)
        ), "mastexto")
      )
    }

    "Deberia funcionar con un grupo" in {
      val str = noWhitespace(
        """color[60, 150, 200](
          |    grupo(
          |   	 triangulo[200 @ 50, 101 @ 335, 299 @ 335],
          |   	 circulo[200 @ 350, 100]
          |    )
          |)""".stripMargin)

      assert(color(str).get == (
        Color(
          Group(List(
            Triangle((200, 50), (101, 335), (299, 335)),
            Circle((200, 350), 100)
          )),
          (60, 150, 200)
        ), "")
      )
    }

    "Deberia fallar si el color no es valido" in {
      val str = noWhitespace(
        """color[60, 150, 300](
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335]
          |)mastexto""".stripMargin)

      assert(color(str).isFailure)
    }

    "Deberia fallar si recibe mas de una figura" in {
      val str = noWhitespace(
        """color[60, 150, 200](
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335],
          |    circulo[200 @ 350, 100]
          |)""".stripMargin)

      assert(color(str).isFailure)
    }

    "Deberia fallar si no esta cerrado" in {
      val str = noWhitespace(
        """color[60, 150, 200(
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335]
          |)mastexto""".stripMargin)

      assert(color(str).isFailure)
    }

    "Deberia poder anidarse" in {
      val str = noWhitespace(
        """color[60, 150, 200](
          |    color[100, 200, 50](
          |   	 triangulo[200 @ 50, 101 @ 335, 299 @ 335]
          |    )
          |)mastexto""".stripMargin)

      assert(color(str).get == (
        Color(
          Color(
            Triangle((200, 50), (101, 335), (299, 335)),
            (100, 200, 50)
          ),
          (60, 150, 200)
        ), "mastexto")
      )
    }
  }


  "scale" - {
    "Deberia poder parsear una escala" in {
      val str = noWhitespace(
        """escala[2.5, 1](
          |  rectangulo[0 @ 100, 200 @ 300]
          |)mastexto""".stripMargin)

      assert(scale(str).get == (
        Scale(
          Rectangle((0, 100), (200, 300)),
          (2.5, 1)
        ), "mastexto")
      )
    }

    "Deberia fallar si no hay dos valores" in {
      val str = noWhitespace(
        """escala[2.5](
          |  rectangulo[0 @ 100, 200 @ 300]
          |)mastexto""".stripMargin)

      assert(scale(str).isFailure)
    }

    "Deberia funcionar con un grupo" in {
      val str = noWhitespace(
        """escala[2.5, 1](
          |  grupo(
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335],
          |    circulo[200 @ 350, 100]
          |  )
          |)""".stripMargin)

      assert(scale(str).get == (
        Scale(
          Group(List(
            Triangle((200, 50), (101, 335), (299, 335)),
            Circle((200, 350), 100)
          )),
          (2.5, 1)
        ), "")
      )
    }
  }

  "rotation" - {
    "Deberia poder parsear una rotacion" in {
      val str = noWhitespace(
        """rotacion[45](
          |  rectangulo[300 @ 0, 500 @ 200]
          |)mastexto""".stripMargin)

      assert(rotation(str).get == (
        Rotation(
          Rectangle((300, 0), (500, 200)),
          45
        ), "mastexto")
      )
    }

    "Deberia falalr si el angulo es mayor a 359" in {
      val str = noWhitespace(
        """rotacion[360](
          |  rectangulo[300 @ 0, 500 @ 200]
          |)mastexto""".stripMargin)

      assert(rotation(str).isFailure)
    }

    "Deberia funcionar con un grupo" in {
      val str = noWhitespace(
        """rotacion[45](
          |  grupo(
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335],
          |    circulo[200 @ 350, 100]
          |  )
          |)""".stripMargin)

      assert(rotation(str).get == (
        Rotation(
          Group(List(
            Triangle((200, 50), (101, 335), (299, 335)),
            Circle((200, 350), 100)
          )),
          45
        ), "")
      )
    }
  }

  "traslation" - {
    "Deberia poder parsear una traslacion" in {
      val str = noWhitespace(
        """traslacion[200, 50](
          |  triangulo[0 @ 100, 200 @ 300, 150 @ 500]
          |)mastexto""".stripMargin)

      assert(traslation(str).get == (
        Traslation(
          Triangle((0, 100), (200, 300), (150, 500)),
          (200, 50)
        ), "mastexto")
      )
    }

    "Deberia fallar si no hay dos valores" in {
      val str = noWhitespace(
        """traslacion[200](
          |  triangulo[0 @ 100, 200 @ 300, 150 @ 500]
          |)mastexto""".stripMargin)

      assert(traslation(str).isFailure)
    }

    "Deberia funcionar con un grupo" in {
      val str = noWhitespace(
        """traslacion[200, 50](
          |  grupo(
          |    triangulo[200 @ 50, 101 @ 335, 299 @ 335],
          |    circulo[200 @ 350, 100]
          |  )
          |)""".stripMargin)

      assert(traslation(str).get == (
        Traslation(
          Group(List(
            Triangle((200, 50), (101, 335), (299, 335)),
            Circle((200, 350), 100)
          )),
          (200, 50)
        ), "")
      )
    }
  }

  "simplificacion" - {
    "Deberia simplificar color anidado" in {
      val str = noWhitespace(
        """color[0, 0, 0](
          |  color[200, 200, 200](
          |    rectangulo[100 @ 100, 200 @ 200]
          |  )
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Color(Rectangle((100, 100), (200, 200)), (200, 200, 200)))
    }

    "Deberia simplificar transformaciones aplicadas a todos los hijos de un grupo" in {
      val str = noWhitespace(
        """grupo(
          |  color[200, 200, 200](
          |    rectangulo[100 @ 100, 200 @ 200]
          |  ),
          |  color[200, 200, 200](
          |    circulo[100 @ 300, 150]
          |  )
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Color(Group(List(
        Rectangle((100, 100), (200, 200)),
        Circle((100, 300), 150)
      )), (200, 200, 200)))
    }

    "Deberia simplificar rotaciones anidadas" in {
      val str = noWhitespace(
        """rotacion[300](
          |  rotacion[10](
          |    rectangulo[100 @ 200, 300 @ 400]
          |  )
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Rotation(Rectangle((100, 200), (300, 400)), 310))
    }

    "Deberia simplificar escalas anidadas" in {
      val str = noWhitespace(
        """escala[2, 3](
          |  escala[3, 5](
          |    circulo[0 @ 5, 10]
          |  )
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Scale(Circle((0, 5), 10), (6, 15)))
    }

    "Deberia simplificar traslaciones anidadas" in {
      val str = noWhitespace(
        """traslacion[100, 5](
          |  traslacion[20, 10](
          |    circulo[0 @ 5, 10]
          |  )
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Traslation(Circle((0, 5), 10), (120, 15)))
    }

    "Deberia eliminar rotacion de 0 grados" in {
      val str = noWhitespace(
        """rotacion[0](
          |  rectangulo[100 @ 200, 300 @ 400]
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Rectangle((100, 200), (300, 400)))
    }

    "Deberia eliminar escala de 1 en x, 1 en y" in {
      val str = noWhitespace(
        """escala[1, 1](
          |  circulo[0 @ 5, 10]
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Circle((0, 5), 10))
    }

    "Deberia eliminar traslacion de 0 en x, 0 en y" in {
      val str = noWhitespace(
        """traslacion[0, 0](
          |  triangulo[0 @ 100, 200 @ 300, 150 @ 500]
          |)""".stripMargin)
      val figure = imageParser(str).get._1
      assert(simplify(figure) == Triangle((0, 100), (200, 300), (150, 500)))
    }
  }
}