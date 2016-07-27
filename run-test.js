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

// Execute test
require("./target/test/test");

process.exit()

