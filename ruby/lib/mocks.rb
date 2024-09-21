# Se incluye en class durante tests
module Mockeable
  def originales
    # Inicializar la lista si no existe
    @originales ||= []
  end

  def mockear(metodo, &bloque)
    originales << self.instance_method(metodo)
    # Sobrescribir el metodo
    self.define_method(metodo, &bloque)
  end

  def desmockear
    originales.each do |metodo|
      self.define_method(metodo.name, &metodo)
    end
  end
end
