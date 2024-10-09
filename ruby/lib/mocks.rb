# Se incluye en class durante tests
module Mockeable
  # Guardamos una lista global de todos los metodos que fueron mockeados.
  def self.originales
    # Inicializar la lista si no existe
    @@originales ||= []
  end

  def mockear(metodo, &bloque)
    raise "El metodo #{metodo} ya fue mockeado previamente" if Mockeable.originales.any? do |original|
      metodo == original.name and self == original.owner
    end

    Mockeable.originales << self.instance_method(metodo)
    # Sobrescribir el metodo
    self.define_method(metodo, &bloque)
  end

  def self.desmockear
    originales.each do |metodo|
      metodo.owner.define_method(metodo.name, metodo)
    end
    # Limpiar la lista
    originales.clear
  end
end
