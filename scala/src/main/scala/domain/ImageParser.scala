package domain

type Point = (Int, Int)

sealed trait Figure

case class Triangle(p1: Point, p2: Point, p3: Point) extends Figure

case class Rectangle(p1: Point, p2: Point) extends Figure

case class Circle(center: Point, radius: Int) extends Figure

case class Group(figures: List[Figure]) extends Figure

// Utilidades
def surroundedBy[T](start: String, end: String, p: Parser[T]): Parser[T] = string(start) ~> p <~ string(end)

// Parsers del lenguaje
val point: Parser[Point] = integer <> (char('@') ~> integer)

val triangle: Parser[Triangle] = surroundedBy("triangulo[", "]",
  point.sepBy(char(','))
    .satisfies(_.length == 3)
    .map(l => Triangle(l.head, l(1), l(2)))
)


val rectangle: Parser[Rectangle] = surroundedBy("rectangulo[", "]",
  point.sepBy(char(','))
    .satisfies(_.length == 2)
    .map(l => Rectangle(l.head, l(1)))
)

val circle: Parser[Circle] = surroundedBy("circulo[", "]",
  (point <~ char(',') <> integer)
    .map(Circle(_, _))
)

lazy val figure: Parser[Figure] = triangle <|> rectangle <|> circle <|> group

lazy val group: Parser[Group] = surroundedBy("grupo(", ")",
  figure.sepBy(char(','))
    .map(Group.apply)
)

