require_relative 'espia'

# Se incluye en Object durante un test
module Testeable
  def deberia(assertion)
    assertion.check(self)
  end
end

class Assertion
  attr_reader :resultado

  def initialize(&bloque)
    @bloque = bloque
  end

  def check(objeto_testeado)
    @resultado = begin
      @bloque.call(objeto_testeado)
    rescue StandardError => e
      Resultado.errors(e)
    end
  end
end

class EspiaAssertion < Assertion
  def initialize(msg)
    @msg = msg
    @veces = 1
    @args = :any
  end

  def veces(numero)
    @veces = numero
    self
  end

  def con_argumentos *args
    @args = args
    self
  end

  def check(espia)
    unless espia.is_a? Espia
      @resultado = Resultado.fails("El objeto #{espia} no es un espia")
      return
    end

    llamados = espia.llamados.select do |llamado|
      llamado.msg == @msg and (@args == :any or llamado.args == @args)
    end

    msg_str = @args == :any ? @msg : "#{@msg}(#{@args.join(', ')})"
    @resultado = Resultado.from_bool(
      llamados.size >= @veces,
      "Se esperaban #{@veces} llamados a #{msg_str}, pero se encontraron #{llamados.size}"
    )
  end
end

# Retornado por los bloques de las Assertions
class Resultado
  attr_reader :tipo, :mensaje, :backtrace

  def initialize(tipo, mensaje = nil, backtrace = nil)
    @tipo = tipo # :passes, :fails, o :errors
    @mensaje = mensaje
    @backtrace = backtrace
  end

  # Abreviaciones
  def self.passes
    Resultado.new(:passes)
  end

  def self.fails(mensaje_error)
    Resultado.new(:fails, mensaje_error)
  end

  def self.errors(exception)
    Resultado.new(:errors, exception.message, exception.backtrace)
  end

  def self.from_bool(bool, mensaje_error)
    bool ? Resultado.passes : Resultado.fails(mensaje_error)
  end
end
