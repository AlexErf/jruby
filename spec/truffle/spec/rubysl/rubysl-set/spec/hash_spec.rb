require 'set'

ruby_version_is "1.8.7" do
  describe "Set#hash" do
    it "is static" do
      Set[].hash.should == Set[].hash
      Set[1, 2, 3].hash.should == Set[1, 2, 3].hash
      Set[:a, "b", ?c].hash.should == Set[?c, "b", :a].hash

      Set[].hash.should_not == Set[1, 2, 3].hash
      Set[1, 2, 3].hash.should_not == Set[:a, "b", ?c].hash
    end
  end
end
