module Testeable
  def deberia asercion
    if self.es_un_test caller_locations(1,1)[0]
      self.deberia_reusable asercion
    else
      puts "Esto no es un test!"
    end
  end

  def deberia_reusable asercion
    self.instance_eval &asercion
  end

  def es_un_test metodo
    metodo.label.to_s.start_with?("testear_que_") and SuiteTest.instance_method(metodo.base_label).arity == 0
  end

end