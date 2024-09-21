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
