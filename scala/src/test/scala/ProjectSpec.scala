import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._

class ProjectSpec extends AnyFreeSpec {

  "Este proyecto" - {

    "cuando está correctamente configurado" - {
      "debería resolver las dependencias y pasar este test" in {
        Prueba.materia shouldBe "tadp"
      }
    }
  }

}
