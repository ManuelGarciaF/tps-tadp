# Se incluye en Object durante un test
module Testeable
  def deberia(assertion)
    assertion.check(self)
  end
end
