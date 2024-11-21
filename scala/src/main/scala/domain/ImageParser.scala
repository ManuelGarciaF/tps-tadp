package domain

type Point = (Double, Double)

sealed trait Figure

case class Triangle(p1: Point, p2: Point, p3: Point) extends Figure

case class Rectangle(p1: Point, p2: Point) extends Figure

case class Circle(center: Point, radius: Int) extends Figure {
  require(radius > 0)
}

case class Group(figures: List[Figure]) extends Figure

// Transformaciones
case class Colour(figure: Figure, color: (Int, Int, Int)) extends Figure {
  require(color._1 >= 0 && color._1 <= 255)
  require(color._2 >= 0 && color._2 <= 255)
  require(color._3 >= 0 && color._3 <= 255)
}

case class Scale(figure: Figure, factor: (Double, Double)) extends Figure {
  require(factor._1 > 0)
  require(factor._2 > 0)
}

case class Rotation(figure: Figure, angle: Double) extends Figure {
  require(angle >= 0 && angle < 360)
}

case class Traslation(figure: Figure, displacement: (Double, Double)) extends Figure

// Utilidades
def surroundedBy[T](start: String, end: String, p: Parser[T]): Parser[T] = string(start) ~> p <~ string(end)

def argList2[T](p: Parser[T]): Parser[(T, T)] = (p <~ char(',')) <> p

def argList3[T](p: Parser[T]): Parser[(T, T, T)] = p.sepBy(char(','))
  .satisfies(_.length == 3)
  .map(l => (l.head, l(1), l(2)))

/*
** Parsers
*/
val point: Parser[Point] = double <> (char('@') ~> double)

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

lazy val color: Parser[Colour] = (surroundedBy("color[", "]", argList3(integer)) <> figureParens)
  .map((rgb, figure) => Colour(figure, rgb))

lazy val scale: Parser[Scale] = (surroundedBy("escala[", "]", argList2(double)) <> figureParens)
  .map((factor, figure) => Scale(figure, factor))

lazy val rotation: Parser[Rotation] = (surroundedBy("rotacion[", "]", double) <> figureParens)
  .map((angle, figure) => Rotation(figure, angle))

lazy val traslation: Parser[Traslation] = (surroundedBy("traslacion[", "]", argList2(double)) <> figureParens)
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
def simplify(root: Figure): Figure = root match {
  // Transformaciones anidadas
  case Colour(Colour(f, color), _) => simplify(Colour(f, color))
  case Rotation(Rotation(f, a1), a2) => simplify(Rotation(f, (a1 + a2) % 360))
  case Scale(Scale(f, f1), f2) => simplify(Scale(f, (f1._1 * f2._1, f1._2 * f2._2)))
  case Traslation(Traslation(f, d1), d2) => simplify(Traslation(f, (d1._1 + d2._1, d1._2 + d2._2)))

  // Transformaciones redundantes
  case Rotation(f, 0) => simplify(f)
  case Scale(f, (1, 1)) => simplify(f)
  case Traslation(f, (0, 0)) => simplify(f)

  // Simplificar grupos
  case Group(fs) => extractTransformations(fs.map(simplify))

  // Simplificar figuras anidadas
  case Colour(f, c) => Colour(simplify(f), c)
  case Scale(f, s) => Scale(simplify(f), s)
  case Rotation(f, a) => Rotation(simplify(f), a)
  case Traslation(f, d) => Traslation(simplify(f), d)

  // Las figuras simples no se pueden simplificar más
  case Triangle(_, _, _) | Rectangle(_, _) | Circle(_, _) => root
}

// Si todos los elementos son transformaciones iguales, simplificar a una sola transformación
def extractTransformations(fs: List[Figure]) = fs.head match {
  // Si la primera figura es una transformación, ver si el resto son iguales
  case Colour(_, c) if fs.forall { case Colour(_, `c`) => true; case _ => false } =>
    Colour(Group(
      fs.collect { case Colour(f, _) => f }
    ), c)

  case Scale(_, s) if fs.forall { case Scale(_, `s`) => true; case _ => false } =>
    Scale(Group(
      fs.collect { case Scale(f, _) => f }
    ), s)

  case Rotation(_, a) if fs.forall { case Rotation(_, `a`) => true; case _ => false } =>
    Rotation(Group(
      fs.collect { case Rotation(f, _) => f }
    ), a)

  case Traslation(_, d) if fs.forall { case Traslation(_, `d`) => true; case _ => false } =>
    Traslation(Group(
      fs.collect { case Traslation(f, _) => f }
    ), d)

  case _ => Group(fs)
}
