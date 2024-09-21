module Espia
  attr_accessor :objeto_espiado

  def llamados
    # Inicializar la lista si no existe.
    @llamados ||= []
  end

  def self.prepended base
    # Por cada metodo de la clase base.
    base.instance_methods.each do |metodo|
      # Crear un metodo que intercepte las llamadas y las guarde.
      base.define_method metodo do |*args|
        llamados << Llamado.new(metodo, args)
        super(*args)
      end
    end
  end

  def fue_llamado_msg? msg
    llamados.any? { |llamado| llamado.msg == msg }
  end
end

class Llamado
  attr_accessor :msg, :args

  def initialize msg, args
    @msg = msg
    @args = args
  end
end
