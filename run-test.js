/*
* This hack is needed so that babel polofils and react-native mock are
* loaded before closure tries to load any react-native dependencies.
* 
* If you try to any test thet refers to react-native you will get an
* error saying that __DEV__ is not defined.
*
* Currently tests are run after test build is run.
*/

require("babel-polyfill");
require("react-native-mock/mock");
require("mocha")
var expect = require("chai").expect
var fetch = require("node-fetch");
global.fetch = fetch;
describe("Ensure that mocha and chai are up and running",
         function() {it("This should pass",
                        function(){
                            expect(true).to.be.ok;
                        })});
// Execute test
require("./target/test/test");

// process.exit()



