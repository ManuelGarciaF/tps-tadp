require_relative '../lib/runner'

class Persona
  attr_accessor :edad

  def initialize(edad)
    @edad = edad
  end

  def viejo?
    edad > 29
  end
end

class TestearAssertions
  def testear_que_puedo_checkear_igualdad
    7.deberia ser 7
    true.deberia ser false
    leandro = Persona.new(22)
    leandro.edad.deberia ser 25
  end

  def testear_que_puedo_checkear_rangos_e_inclusion
    leandro = Persona.new(22)
    leandro.edad.deberia ser mayor_a 20
    leandro.edad.deberia ser menor_a 25
    leandro.edad.deberia ser uno_de_estos [7, 22, 'hola']
    leandro.edad.deberia ser uno_de_estos 7, 22, 'hola'
  end

  def testear_que_puedo_usar_ser
    nico = Persona.new(30)
    nico.deberia ser_viejo
    nico.viejo?.deberia ser true

    leandro = Persona.new(22)
    leandro.deberia ser_viejo
    leandro.deberia ser_joven
  end

  def testear_que_puedo_usar_tener
    leandro = Persona.new(22)
    leandro.deberia tener_edad 22
    leandro.deberia tener_nombre 'leandro'
    leandro.deberia tener_nombre nil
    leandro.deberia tener_edad mayor_a 20
    leandro.deberia tener_edad uno_de_estos [7, 22, 'hola']
  end

  def testear_que_puedo_checkear_si_entiende_mensajes
    leandro = Persona.new(22)
    leandro.deberia entender :viejo?
    leandro.deberia entender :class
    leandro.deberia entender :nombre
  end

  def testear_que_un_bloque_explota_con_un_error
    leandro = Persona.new(22)
    en { 7 / 0 }.deberia explotar_con ZeroDivisionError
    en { leandro.nombre }.deberia explotar_con NoMethodError
    en { leandro.nombre }.deberia explotar_con Exception # 'Error' no existe?
    en { leandro.viejo? }.deberia explotar_con NoMethodError
    en { 7 / 0 }.deberia explotar_con NoMethodError
  end

  def metodo_que_no_es_un_test
    puts 'hola mundo!'
  end
end

class PersonaHome
  def todas_las_personas
    # Este método consume un servicio web que consulta una base de datos
  end

  def personas_viejas
    todas_las_personas.select { |p| p.viejo? }
  end
end

class PersonaHomeTests
  def testear_que_personas_viejas_trae_solo_a_los_viejos
    nico = Persona.new(30)
    axel = Persona.new(30)
    lean = Persona.new(22)
    # Mockeo el mensaje para no consumir el servicio y simplificar el test
    PersonaHome.mockear(:todas_las_personas) do
      [nico, axel, lean]
    end
    viejos = PersonaHome.new.personas_viejas
    viejos.deberia ser [nico, axel]
  end

  def testear_que_se_remueve_el_mock
    PersonaHome.new.todas_las_personas.deberia ser nil
  end
end

class OtraSuite
  def testear_que_corren_multiples_suites
    42.deberia ser uno_de_estos [42]
  end
end

class PersonaTest
  def testear_que_se_use_la_edad
    Persona.new(22)
    pato = Persona.new(23)
    pato = espiar(pato)
    pato.viejo?
    pato.deberia haber_recibido(:edad)
    pato.deberia haber_recibido(:edad).veces(1) # pasa: edad se recibió exactamente 1 vez.
    pato.deberia haber_recibido(:edad).veces(5) # falla: edad sólo se recibió una vez.
    pato.deberia haber_recibido(:viejo?).con_argumentos(19, 'hola') # falla, recibió el mensaje, pero sin esos argumentos.
    pato.deberia haber_recibido(:viejo?).con_argumentos # pasa, recibió el mensaje sin argumentos.

    lean = Persona.new(6666)
    lean.viejo?
    lean.deberia haber_recibido(:edad) # falla: lean no fue espiado!
  end
end

# TADsPec.testear TestearAssertions, :puedo_checkear_rangos_e_inclusion
# TADsPec.testear OtraSuite
TADsPec.testear
