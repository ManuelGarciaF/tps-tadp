class Persona
  attr_accessor :edad

  def initialize(edad)
    @edad = edad
  end

  def viejo?
    edad > 29
  end
end

describe 'Assertions' do
  it 'Deberia permitir checkear igualdad' do
    suite = Class.new do
      def testear_que_funciona
        42.deberia ser 42
      end

      def testear_que_falla
        true.deberia ser false
      end

      def testear_que_funciona_con_atributos
        leandro = Persona.new(22)
        leandro.edad.deberia ser 25
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
    expect(resultados).to include([:testear_que_funciona_con_atributos, test_que_falla])
  end

  it 'Deberia permitir checkear rangos' do
    suite = Class.new do
      def testear_que_funciona
        leandro = Persona.new(22)
        leandro.edad.deberia ser mayor_a 20
        leandro.edad.deberia ser menor_a 25
      end

      def testear_que_falla
        leandro = Persona.new(22)
        leandro.edad.deberia ser menor_a 20
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end

  it 'Deberia permitir checkear inclusion' do
    suite = Class.new do
      def testear_que_funciona_con_listas
        leandro = Persona.new(22)
        leandro.edad.deberia ser uno_de_estos [7, 22, 'hola']
      end

      def testear_que_funciona_con_multiples_parametros
        leandro = Persona.new(22)
        leandro.edad.deberia ser uno_de_estos 7, 22, 'hola'
      end

      def testear_que_falla
        leandro = Persona.new(42)
        leandro.edad.deberia ser uno_de_estos 7, 22, 'hola'
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona_con_listas, test_que_pasa])
    expect(resultados).to include([:testear_que_funciona_con_multiples_parametros, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end

  it 'Deberia permitir checkear inclusion' do
    suite = Class.new do
      def testear_que_funciona_con_listas
        leandro = Persona.new(22)
        leandro.edad.deberia ser uno_de_estos [7, 22, 'hola']
      end

      def testear_que_funciona_con_multiples_parametros
        leandro = Persona.new(22)
        leandro.edad.deberia ser uno_de_estos 7, 22, 'hola'
      end

      def testear_que_falla
        leandro = Persona.new(42)
        leandro.edad.deberia ser uno_de_estos 7, 22, 'hola'
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona_con_listas, test_que_pasa])
    expect(resultados).to include([:testear_que_funciona_con_multiples_parametros, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end

  it 'Deberia poder usar la abreviacion de ser_algo' do
    suite = Class.new do
      def testear_que_funciona
        nico = Persona.new(30)
        nico.deberia ser_viejo
      end

      def testear_que_falla
        leandro = Persona.new(22)
        leandro.deberia ser_viejo
      end

      def testear_que_explota
        leandro = Persona.new(22)
        leandro.deberia ser_joven
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
    expect(resultados).to include([:testear_que_explota, test_que_explota])
  end

  it 'Deberia poder usar tener_atributo' do
    suite = Class.new do
      def testear_que_funciona_con_igualdad
        leandro = Persona.new(22)
        leandro.deberia tener_edad 22
      end

      def testear_que_falla
        leandro = Persona.new(22)
        leandro.deberia tener_edad 25
      end

      def testear_que_funciona_con_atributos_que_no_existen
        leandro = Persona.new(22)
        leandro.deberia tener_nombre nil
      end

      def testear_que_funciona_con_otros_criterios
        leandro = Persona.new(22)
        leandro.deberia tener_edad mayor_a 20
        leandro.deberia tener_edad uno_de_estos [7, 22, 'hola']
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona_con_igualdad, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
    expect(resultados).to include([:testear_que_funciona_con_atributos_que_no_existen, test_que_pasa])
    expect(resultados).to include([:testear_que_funciona_con_otros_criterios, test_que_pasa])
  end

  it 'Deberia poder checkear que entienda mensajes' do
    suite = Class.new do
      def testear_que_funciona_con_metodos_propios
        leandro = Persona.new(22)
        leandro.deberia entender :viejo?
      end

      def testear_que_funciona_con_metodos_heredados
        leandro = Persona.new(22)
        leandro.deberia entender :class
      end

      def testear_que_falla
        leandro = Persona.new(22)
        leandro.deberia entender :nombre
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona_con_metodos_propios, test_que_pasa])
    expect(resultados).to include([:testear_que_funciona_con_metodos_heredados, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end

  it 'Deberia poder ver que un bloque explota' do
    suite = Class.new do
      def testear_que_funciona_con_un_error_exacto
        en { 7 / 0 }.deberia explotar_con ZeroDivisionError
        leandro = Persona.new(22)
        en { leandro.nombre }.deberia explotar_con NoMethodError
      end

      def testear_que_funciona_con_un_error_mas_general
        leandro = Persona.new(22)
        en { leandro.nombre }.deberia explotar_con Exception # 'Error' no existe?
      end

      def testear_que_falla
        leandro = Persona.new(22)
        en { leandro.viejo? }.deberia explotar_con NoMethodError # Leandro entiende 'viejo?'
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona_con_un_error_exacto, test_que_pasa])
    expect(resultados).to include([:testear_que_funciona_con_un_error_mas_general, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end
end

describe 'Mocks' do
  class PersonaHome
    def todas_las_personas
      # Este método consume un servicio web que consulta una base de datos
    end

    def personas_viejas
      todas_las_personas.select { |p| p.viejo? }
    end
  end

  it 'Deberia poder mockear un metodo' do
    suite = Class.new do
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

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_personas_viejas_trae_solo_a_los_viejos, test_que_pasa])
    expect(resultados).to include([:testear_que_se_remueve_el_mock, test_que_pasa])
  end
end

describe 'Espias' do
  it 'Deberia poder ver si se llamo un metodo' do
    suite = Class.new do
      def testear_que_funciona
        pato = Persona.new(23)
        pato = espiar(pato)
        pato.viejo?
        pato.viejo?
        pato.deberia haber_recibido(:edad)
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona, test_que_pasa])
  end

  it 'Deberia poder ver si se llamo cierta cantidad de veces' do
    suite = Class.new do
      def testear_que_funciona
        pato = Persona.new(23)
        pato = espiar(pato)
        5.times do
          pato.viejo?
        end
        pato.deberia haber_recibido(:edad).veces(5)
      end

      def testear_que_falla
        pato = Persona.new(23)
        pato = espiar(pato)
        pato.viejo?
        pato.deberia haber_recibido(:edad).veces(5)
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end

  it 'Deberia poder ver si se llamo con los argumentos correctos' do
    suite = Class.new do
      def testear_que_funciona
        pato = Persona.new(23)
        pato = espiar(pato)
        pato.viejo?
        pato.deberia haber_recibido(:viejo?).con_argumentos # pasa, recibió el mensaje sin argumentos.
      end

      def testear_que_falla
        pato = Persona.new(23)
        pato = espiar(pato)
        pato.viejo?
        pato.deberia haber_recibido(:viejo?).con_argumentos(19, 'hola')
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_funciona, test_que_pasa])
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end

  it 'Deberia fallar si un objeto no fue espiado' do
    suite = Class.new do
      def testear_que_falla
        lean = Persona.new(34)
        lean.viejo?
        lean.deberia haber_recibido(:edad) # falla: lean no fue espiado!
      end
    end

    resultados = correr_suite(suite)
    expect(resultados).to include([:testear_que_falla, test_que_falla])
  end
end
