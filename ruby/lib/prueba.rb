require_relative 'object'
require_relative 'testeable'
require_relative 'test'

class Persona
  attr_accessor :nombre, :edad
  def initialize(nombre, edad)
    @nombre = nombre
    @edad = edad
  end
end

class SuiteTest
  include Test

  def testear_que_funcione
    7.class.include Testeable
    # true.class.include Testeable
    # false.class.include Testeable
    #7.deberia ser menor_a  8
    7.deberia ser uno_de_estos [6,  8]
    #7.deberia ser_even

  end

  def testear_que_funcione_persona
    Persona.include Testeable
    7.class.include Testeable
    juan = Persona.new("Juan", 30)
    juan.deberia tener_edad uno_de_estos [32, 31, 30]
    #juan.deberia tener_edad mayor_a 30
  end

  def testear_que_funcione_entender
    Persona.include Testeable
    7.class.include Testeable
    #7.deberia entender :even?
    7.deberia entender :nombre?
  end

  def testear_que_explote
    Proc.include Testeable
    en {7/0}.deberia explotar_con Object
  end

end
