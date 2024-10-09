require 'rspec'

require_relative '../lib/runner'

# Definir matchers para checkear resultados mas facil
RSpec::Matchers.define :test_que_pasa do
  match { |instancia_suite| instancia_suite.resultado == :passes }
end
RSpec::Matchers.define :test_que_falla do
  match { |instancia_suite| instancia_suite.resultado == :fails }
end

RSpec::Matchers.define :test_que_explota do
  match { |instancia_suite| instancia_suite.resultado == :errors }
end

def correr_suite(suite)
  # Agregar los modulos necesarios para testear, hay que hacerlo manualmente,
  # ya que no usamos TADsPec.testear.
  Object.include Testeable
  Class.include Mockeable
  TADsPec.correr_suite(suite)
end
