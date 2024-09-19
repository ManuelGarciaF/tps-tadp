require_relative 'testeable'

class ClaseConProc
  include Testeable
  attr_accessor :procAttr
  def initialize(unProc)
    @procAttr = unProc
  end

end
class Object
  def en &bloque
    if block_given?
      ClaseConProc.new(bloque)
    end
  end
end

