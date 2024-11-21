import tadp.drawing.TADPDrawingAdapter
import domain.*
import scalafx.scene.paint.Color

val ejemplo = """
escala[1.45, 1.45](
 grupo(
   color[0, 0, 0](
     rectangulo[0 @ 0, 400 @ 400]
   ),
   color[200, 70, 0](
     rectangulo[0 @ 0, 180 @ 150]
   ),
   color[250, 250, 250](
     grupo(
       rectangulo[186 @ 0, 400 @ 150],
       rectangulo[186 @ 159, 400 @ 240],
       rectangulo[0 @ 159, 180 @ 240],
       rectangulo[45 @ 248, 180 @ 400],
       rectangulo[310 @ 248, 400 @ 400],
       rectangulo[186 @ 385, 305 @ 400]
    )
   ),
   color[30, 50, 130](
       rectangulo[186 @ 248, 305 @ 380]
   ),
   color[250, 230, 0](
       rectangulo[0 @ 248, 40 @ 400]
   )
 )
)
"""

def noWhitespace(s: String): String = s.filter(!_.isWhitespace)

object Dibujar extends App {
  TADPDrawingAdapter.forScreen { adapter =>
    val ast = imageParser(noWhitespace(ejemplo)).get._1
    val astSimplificado = simplify(ast)

    draw(astSimplificado, adapter)
  }
}

object GUIDeTextoADibujo extends App {

  TADPDrawingAdapter.forInteractiveScreen { (texto, adapter) =>
    println("Parseando...")
    val ast = imageParser(noWhitespace(texto)).get._1
    println("Simplificando...")
    val astSimplificado = simplify(ast)

    println("Dibujando...")
    draw(astSimplificado, adapter)
  }
}

object GenerarImagen extends App {
  // La imagen se genera en la carpeta out/
  TADPDrawingAdapter.forImage("imagen.png") { adapter =>
        val ast = imageParser(noWhitespace(ejemplo)).get._1
        val astSimplificado = simplify(ast)

        draw(astSimplificado, adapter)
  }
}

def draw(figure: Figure, adapter: TADPDrawingAdapter): TADPDrawingAdapter = {
  figure match {
    case Triangle(p1, p2, p3) => adapter.triangle(p1, p2, p3)
    case Rectangle(p1, p2) => adapter.rectangle(p1, p2)
    case Circle(c, r) => adapter.circle(c, r)

    case Group(fs) => fs.foldLeft(adapter)((a, f) => draw(f, a))

    case Colour(f, c) => draw(f, adapter.beginColor(Color.rgb(c._1, c._2, c._3))).end()
    case Scale(f, s) => draw(f, adapter.beginScale(s._1, s._2)).end()
    case Rotation(f, a) => draw(f, adapter.beginRotate(a)).end()
    case Traslation(f, d) => draw(f, adapter.beginTranslate(d._1, d._2)).end()
  }
}