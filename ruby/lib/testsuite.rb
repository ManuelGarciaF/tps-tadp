require_relative 'assertions'

# Se incluye en la clase que ejecute los tests
module TestSuite
  # Usamos una instancia nueva de la clase por test.
  # Guardamos los asserts y sus resultados en la clase.
  def assertions
    # Inicializar la lista si no existe
    @assertions ||= []
  end

  # Reducir el resultado del test a un solo valor
  def resultado
    resultados = assertions.map { |a| a.resultado.tipo }

    return :errors if resultados.include? :errors
    return :fails if resultados.include? :fails

    :passes
  end

  def resultados_fallos
    assertions.select { |a| a.resultado.tipo == :fails }.map(&:resultado)
  end

  def resultados_errores
    assertions.select { |a| a.resultado.tipo == :errors }.map(&:resultado)
  end

  def ser(expected)
    # El valor puede ser un objeto a comparar directamente o otra aserción
    assertion = if expected.is_a?(Assertion)
                  expected
                else
                  Assertion.new do |testeado|
                    Resultado.from_bool(
                      testeado == expected,
                      "Se esperaba #{expected.inspect} pero se obtuvo #{testeado.inspect}"
                    )
                  end
                end

    assertions << assertion
    assertion
  end

  # Los metodos que se usan despues de ser o tener_ retornan lambdas
  def mayor_a(valor)
    Assertion.new do |testeado|
      Resultado.from_bool(
        testeado > valor,
        "Se esperaba un valor mayor a #{valor} pero se obtuvo #{testeado}"
      )
    end
  end

  def menor_a(valor)
    Assertion.new do |testeado|
      Resultado.from_bool(
        testeado < valor,
        "Se esperaba un valor menor a #{valor} pero se obtuvo #{testeado}"
      )
    end
  end

  def uno_de_estos(valores, *otros)
    # Si hay mas de 1 argumento, considerarlos como una lista
    valores = [valores] + otros if otros.length > 0
    Assertion.new do |testeado|
      Resultado.from_bool(
        valores.include?(testeado),
        "Se esperaba uno de los valores #{valores.inspect} pero se obtuvo #{testeado.inspect}"
      )
    end
  end

  def entender(mensaje)
    assertion = Assertion.new do |testeado|
      Resultado.from_bool(
        testeado.respond_to?(mensaje),
        "Se esperaba que #{testeado.inspect} entienda el mensaje #{mensaje}"
      )
    end
    assertions << assertion
    assertion
  end

  # Basicamente un alias de proc
  def en(&bloque)
    bloque
  end

  def explotar_con(error)
    assertion = Assertion.new do |bloque_testeado|
      bloque_testeado.call
      Resultado.fails("Se esperaba que el bloque explote con #{error}")
    rescue error
      Resultado.passes
    end
    assertions << assertion
    assertion
  end

  def method_missing(nombre_msg, *args, &bloque)
    if nombre_msg.to_s.start_with? 'ser_' and args.length == 0
      ser_abreviado(nombre_msg)
    elsif nombre_msg.to_s.start_with? 'tener_' and args.length >= 1
      tener(nombre_msg, args[0])
    else
      super
    end
  end

  def respond_to_missing?(nombre_msg, include_private = false)
    nombre_msg.to_s.start_with? 'ser_' or
      nombre_msg.to_s.start_with? 'tener_' or
      super
  end

  def ser_abreviado(nombre_msg)
    metodo = nombre_msg.to_s.sub('ser_', '').concat('?').to_sym

    assertion = Assertion.new do |testeado|
      Resultado.from_bool(
        testeado.send(metodo),
        "Se esperaba que #{testeado.inspect} sea #{metodo}, pero no lo es"
      )
    end
    assertions << assertion
    assertion
  end

  def tener(nombre_msg, argumento)
    atributo = nombre_msg.to_s.sub('tener_', '').to_sym

    assertion = Assertion.new do |objeto_testeado|
      # Obtener el valor del atributo
      valor_testeado = objeto_testeado.instance_variable_get("@#{atributo}")

      # Si el argumento es un bloque, ejecutarlo con el valor testeado nuevo
      if argumento.is_a? Assertion
        argumento.check(valor_testeado)
      else
        # Si el argumento es un objeto, compararlos
        Resultado.from_bool(
          valor_testeado == argumento,
          "Se esperaba que #{objeto_testeado.inspect} tenga #{atributo} igual a #{argumento} pero se obtuvo #{valor_testeado.inspect}"
        )
      end
    end
    assertions << assertion
    assertion
  end

  def espiar objeto_espiado
    objeto_espiado.singleton_class.prepend Espia
    objeto_espiado
  end

  def haber_recibido msg
    assertion = EspiaAssertion.new msg
    assertions << assertion
    assertion
  end
end
