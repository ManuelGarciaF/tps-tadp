require 'rainbow/refinement'
using Rainbow

require_relative 'assertions'
require_relative 'mocks'

module TADsPec
  def self.testear(clase_suite = nil, *nombre_tests)
    # Agregar el metodo deberia a los objetos
    Object.include Testeable
    Class.include Mockeable

    if clase_suite.nil? # Correr todas las suites
      todas_las_suites.each do |suite|
        resultados = correr_suite(suite)
        imprimir_resultados(suite, resultados)
      end
    else # Correr una suite en particular, posiblemente con tests específicos
      resultados = correr_suite(clase_suite, nombre_tests)
      imprimir_resultados(clase_suite, resultados)
    end
  end

  def self.correr_suite(clase_suite, nombre_tests = [])
    nombre_tests = if nombre_tests.empty?
                     tests_de(clase_suite)
                   else
                     nombre_tests.map { |n| n.to_s.prepend('testear_que_').to_sym }
                   end

    # Agregamos el mixin a la clase que tiene los tests
    clase_suite.include TestSuite

    nombre_tests.map do |nombre_test|
      instancia = clase_suite.new

      # Ejecutar el test
      instancia.send nombre_test

      clase_suite.desmockear

      # Cada test se ejecuta en una instancia nueva, la guardamos para acceder
      # a los resultados luego
      [nombre_test, instancia]
    end

  end

  def self.imprimir_resultados(clase_suite, resultados)
    pasan = resultados.select { |_, i| i.resultado == :passes }
    fallan = resultados.select { |_, i| i.resultado == :fails }
    explotan = resultados.select { |_, i| i.resultado == :errors }

    puts "Resultados de la suite #{clase_suite.inspect}:".bold.blue
    puts "Pasan: #{pasan.length}".green + " Fallan: #{fallan.length}".yellow + " Explotan: #{explotan.length}".red
    puts 'Tests que pasaron:'.green
    pasan.each { |r| puts "\t#{r[0]}".green }

    fallan.each do |nombre, instancia_clase|
      puts "#{nombre} falló con los siguientes errores:".yellow
      instancia_clase.resultados_fallos.each { |r| puts "\t#{r.mensaje}" }
    end

    explotan.each do |nombre, instancia_clase|
      puts "#{nombre} explotó con los siguientes errores:".red
      instancia_clase.resultados_errores.each do |r|
        puts "\t#{r.mensaje}"
        puts "\t\t#{r.backtrace.join("\n\t\t")}".red
      end
    end
  end

  def self.todas_las_suites
    Object.constants
          .map { |nombre| Object.const_get nombre }
          .select do |clase|
      clase.is_a? Class and clase.instance_methods.any? { |m| es_un_test(m) }
    end
  end

  def self.tests_de(clase_suite)
    clase_suite.instance_methods
               .select { |m| es_un_test(m) }
  end

  def self.es_un_test(nombre)
    nombre.to_s.start_with? 'testear_que_'
  end
end
