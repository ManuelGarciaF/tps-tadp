module Test
  def ser esperado
    if esperado.is_a? Proc
      proc do
        self.instance_eval &esperado
      end
    else
      proc { self == esperado }
    end
  end

  def mayor_a esperado
    proc do
      self > esperado
    end
  end

  def menor_a esperado
    proc do
      self < esperado
    end
  end

  def uno_de_estos * lista
    proc do
      lista.flatten.include?(self)
    end
  end

  def entender simbolo
    proc { self.respond_to? simbolo }
  end

  def explotar_con error
    proc do
      begin
        if self.instance_variable_get(:@procAttr).call
          false
        end
      rescue error
        true
      rescue
        false
      end
    end
  end

  def method_missing(name, *args)
    if self.empieza_con name, "ser"
      proc { self.instance_eval &(name.to_s.split('_', 2).last + '?').to_sym }
    elsif self.empieza_con name, "tener"
      string_sin_tener = name.to_s.split('_', 2).last
      proc do
        valor_atributo = self.instance_variable_get("@#{string_sin_tener}")
        SuiteTest.new.instance_exec(valor_atributo, *args) do |valor, *args|
          valor.deberia_reusable ser(args[0])
        end
      end
    else
      super
    end
  end

  def respond_to_missing?(name, include_private)
    self.empieza_con name, "ser" or self.empieza_con name, "tener"
  end

  def empieza_con method_name, comienzo
    method_name.to_s.start_with? comienzo
  end

end