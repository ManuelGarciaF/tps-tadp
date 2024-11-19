package domain

type Point = (Int, Int)

sealed trait Figure

case class Triangle(p1: Point, p2: Point, p3: Point) extends Figure

case class Rectangle(p1: Point, p2: Point) extends Figure

case class Circle(center: Point, radius: Int) extends Figure {
  require(radius > 0)
}

case class Group(figures: List[Figure]) extends Figure

sealed trait Transformation extends Figure {
  val figure: Figure
}

// Transformaciones
case class Color(figure: Figure, color: (Int, Int, Int)) extends Transformation {
  require(color._1 >= 0 && color._1 <= 255)
  require(color._2 >= 0 && color._2 <= 255)
  require(color._3 >= 0 && color._3 <= 255)
}

case class Scale(figure: Figure, factor: (Double, Double)) extends Transformation {
  require(factor._1 > 0)
  require(factor._2 > 0)
}

case class Rotation(figure: Figure, angle: Int) extends Transformation {
  require(angle >= 0 && angle < 360)
}

case class Traslation(figure: Figure, displacement: (Int, Int)) extends Transformation

// Utilidades
def surroundedBy[T](start: String, end: String, p: Parser[T]): Parser[T] = string(start) ~> p <~ string(end)

def argList2[T](p: Parser[T]): Parser[(T, T)] = (p <~ char(',')) <> p

def argList3[T](p: Parser[T]): Parser[(T, T, T)] = p.sepBy(char(','))
  .satisfies(_.length == 3)
  .map(l => (l.head, l(1), l(2)))

/*
** Parsers
*/
val point: Parser[Point] = integer <> (char('@') ~> integer)

// Figuras basicas
val triangle: Parser[Triangle] = surroundedBy("triangulo[", "]",
  argList3(point).map(l => Triangle(l._1, l._2, l._3))
)

val rectangle: Parser[Rectangle] = surroundedBy("rectangulo[", "]",
  argList2(point).map(l => Rectangle(l._1, l._2))
)

val circle: Parser[Circle] = surroundedBy("circulo[", "]",
  (point <~ char(',') <> integer)
    .map(Circle(_, _))
)

// Transformaciones
lazy val figureParens = surroundedBy("(", ")", figure)

lazy val color: Parser[Color] = (surroundedBy("color[", "]", argList3(integer)) <> figureParens)
  .map((rgb, figure) => Color(figure, rgb))

lazy val scale: Parser[Scale] = (surroundedBy("escala[", "]", argList2(double)) <> figureParens)
  .map((factor, figure) => Scale(figure, factor))

lazy val rotation: Parser[Rotation] = (surroundedBy("rotacion[", "]", integer) <> figureParens)
  .map((angle, figure) => Rotation(figure, angle))

lazy val traslation: Parser[Traslation] = (surroundedBy("traslacion[", "]", argList2(integer)) <> figureParens)
  .map((displacement, figure) => Traslation(figure, displacement))

// Grupos
lazy val group: Parser[Group] = surroundedBy("grupo(", ")",
  figure.sepBy(char(','))
    .map(Group.apply)
)

lazy val figure: Parser[Figure] = triangle
  <|> rectangle
  <|> circle
  <|> group
  <|> color
  <|> scale
  <|> rotation
  <|> traslation

// Parser principal
val imageParser: Parser[Figure] = figure

// Simplificar el AST creado por el parser, recorriendo el arbol recursivamente.
def simplify(f: Figure): Figure = f match {
  case Color(Color(figure, color), _) => simplify(Color(figure, color))
  case Rotation(Rotation(figure, a1), a2) => simplify(Rotation(figure, (a1 + a2) % 360))
  case Scale(Scale(figure, f1), f2) => simplify(Scale(figure, (f1._1 * f2._1, f1._2 * f2._2)))
  case Traslation(Traslation(figure, d1), d2) => simplify(Traslation(figure, (d1._1 + d2._1, d1._2 + d2._2)))

  case Rotation(figure, 0) => simplify(figure)
  case Scale(figure, (1, 1)) => simplify(figure)
  case Traslation(figure, (0, 0)) => simplify(figure)

  // TODO: regla de simplificación para grupos

  case Group(figures) => Group(figures.map(simplify))

  // Si no se cumple ninguna regla, devolver la figura sin modificar
  case _ => f
}