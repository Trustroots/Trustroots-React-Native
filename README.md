[Trustroots mobile app](https://www.trustroots.org/)
=================================

_Travellers' community. Sharing, hosting and getting people together._

![Trustroots logo](https://raw.githubusercontent.com/Trustroots/trustroots/master/modules/core/client/img/icons/android/192.png)


Stack
----------------------------------

- React Native 0.30
- Re-natal
- Clojure/ClojureScript (1.9.x)
  - Reagent
  - Re-frame
  - Prismatic/schema
- For testing:
  - mocha.js and chai.js (with a spec wrapper helper in tests)

See Archictecture in nutshell section for more details on stack.

Configure development enviroment
--------------------------------

### Prequisites

1. Install [React Native](https://facebook.github.io/react-native/docs/getting-started.html) and everything that react native requires for development.
2. Install [Leiningen](http://leiningen.org/#install)
3. Install re-natal: `npm install -g re-natal`
4. Install the Android SDK
5. Create an *Android Virtual Device*. You can use the build-in [emulator](https://developer.android.com/studio/run/emulator.html) from the Android SDK.

In working directory:

1. Install npm dependencies: `npm install`


Develop
---------------------------------

### Development build when using android avd

Run first:

```
re-natal use-android-device avd
re-natal enable-source-maps
re-natal use-figwheel
lein figwheel android
```

Start your development server:

```
npm start
```

Now start up your android virtual device. Then run

```
react-native run-android
```

to install the app to the device.

For more comprehensive instructions see: [Re-natal's readme](https://github.com/drapanjanas/re-natal)

### Run tests

Run tests once by running:

```
lein cljsbuild test
```

Start auto test runner:

```
lein cljsbuild auto test
```

TODO: use [Enzyme to mock React Native]( https://github.com/airbnb/enzyme/blob/master/docs/guides/react-native.md)

### Add modules. images and dependencies

- After you have added a **clojure dependency**:
  - Restart figwheel `lein figwheel android` and restart React Packager `react-native run-android`
- After you have added a **npm dependency**:
  - Add dependency to .re-natal file in modules array. Otherwise it won't be included in the package when using figwheel.
  - Run `re-natal use-figwheel`
  - Restart figwheel `lein figwheel android` and restart React Packager `react-native run-android`
- After you have added an **image** to images folder:
  - Run `re-natal use-figwheel`
  - Restart figwheel `lein figwheel android` and restart React Packager `react-native run-android`


Troubleshooting
------------------

### Problem: React packages gives errors relating missing platform and UI does not update as it should.

Solution:

Patches RN packager to server \*.map files from filesystem, so that chrome can download them by running:

```
re-natal enable-source-maps
re-natal use-figwheel
lein figwheel android
```
And once figwheel have started:

```
react-native run-android
```

### I get an compilation error and I have no clue what's the problem.

Solution 1: In figwheel REPL run:

```
(build-once)
```

You should get more informative error message.

Solution 2: Open develper menu by keyboard short cut `Command ⌘ + M` and click "Enable remote debuging" in Android emulator. This opens browser. Open console in browser - it often gives you better idea what is happening.

Solution 3: Check Android logs using command `adb logcat`. (You probable get insane amount of log entiries, and thus you might want to clear log by `adb logcat -c`

Architecture in nutshell
-----------------------------

Re-frame is a archictural framework that reminds Redux and other Flux implementations. 

The idea is simple:

1. In re-frame application state is stored in an ratom called db (database). Ratom (reactive atom) is an observable datastructure that ensure atomic modifications (i.e. only one change at time to the latest version of data it contains). The sturucture of db is constrained by a schema. Currently plumatic schema is used, but it will change to core.spec at some point.
2. Database is modified by events. The logic how an event changes db is specifed by a handler.
3. UI component listens re-frame subscription. Each subscription is a observable view to db. You need to specify view to data in subs.cljs first, then you can subscribe it in UI component by using `re-frame.core/subscribe` function that returns an `ratom`.
4. UI component will dispatch events to re-frame dispatch. The events are handled by a handler in order they arrive to the dispatcher sequentially. If you dispatch an event in a handler, it will be handled *after* the current handler function call is finished. (I.e. you cannot dispatch event synchronously in a handler.) Each handler must return an updated version of db, and as they're called they get curent db as parameter. After a handler returns, subscriptions trigger.

In most cases you need to do you need to answer three question when you develop new feature:

1. What data feature needs? 
  #. You may need to extend schema and db. 
  #. You need to write a subscrition or maybe two. UI components should not depend directly to db-structure.
2. How data changes?
  #. Implement a handler for each reason to change.
  #. Re-frame encourages you to think how the data changes. Like if data should be changed by a REST call, you probably need a handler for successfull call and an unsuccessful call. That's where you start. Then you probably add handler that does the REST call and changes db so that user knows that something is happening. Handlers for successful or erroneous event handlers do not know where the data related to an event comes from. Handler that does the REST request, doesn't know how the response is used. Pros of this approach is that you can easily change any of the parts without modifying other parts (open/closed principle) and easily trigger any change from REPL or in tests; cons for this this approach is that it can be hard to understand how the data flows and where it comes.
3. When data should be fetched? There are three options: 
  #. When app starts. In this case you dispatch fetch event in [platform]/core.clsj init function. There is separate core.clsj for Androind and iOS in. 
  #. When user does something you dispatch the event from React native UI component event handler.
  #. Periodically (e.g. once per 1 minute). You can use JavaScript setTimeout and put it to handler.cljs file. Currently this is a bit clumsy and problably a helper function should be created for it. 
4. How user interact with data (UI)? 
  #. The goal is that there is as much shared components as possible. UI components are located in `shared`, `android` and `ios` directories. In addition to documentation there is now way to know what component works in which framework. Not all components supports all features in React Native component and component may behave differently on differnt platforms. This is React Native design desision.
5. What component should looklike?
  #. Till now we have used a lot of inline style. They should be refactored to platform specific styles.cljs file.

### Project structure

Actual source code you should modify are in ./src/trustroots/ and ./test/ directories, and images are in /images/ directory. In addition to those there are two files you need to modify:

- package.json contains npm packages.
- .re-natal is used by leiningen to bundle JavaScript source for react-native. You need to list there all npm packages that should be included in react native app. Leiningen does not use package.json for this. I.e. if you want to use an 3rd party library you need install it first by using npm and then add the name of packages to this file. This file contains also some other build related information, and it is modified e.g. when you swap between emulator and physical machine.

#### src/trustroots structure:

- android: android specific UI components goes to this folder
- domain: contains schema definitions.
- ios: iOS specific UI components goes to this folder. Currently app does not support iOS so there's not much here.
- shared: all component thar should work both in android and iOS are located here.
- api.cljs: REST api call helpers goes here.
- db.cljs: This file contains helpers for persisting data, including caching mechanims.
- fetch_helper.cljs contain wrapper for react native fetch function. This is used for REST calls.
- handlers.clsj contains all re-frame handlers. Currently this file is rather long and it might be split to many files.
- subs.cljs contains observable re-frame subscriptions.

### App state (db)scheama 

Currently domain model reflects closely to what REST APIs return. This need to be changed as the app should support off-line use and rest APIs may not return all data (they page data). 

#### Proposall for new schema

- users: map of all users current user has had a conversation, or that are his friends. 
  -  { <id>: <user-data> }
- me: Current user id
- last-sync-time: last change timestamp (this should come form server)
- inbox: {
   *thread*: list of messages
}
- current-page: vector, e.g. [:discussion thread]
- errors: vector of errors
- services: {
   :toaster obj
   :fetch obj
}

#### Other notes

* In future Plumatic schema should be changed to core.spec.

### Handlers

- Initialization 
  - `[:initialize-db]` loads intial version of database.
  - `[:register-service service-obj]` Register helper function for dependency injection.
- Navigation
  - `[:set-page page]` Change UI view.

- Authentication related
  - `[:auth/login {:user username :pwd password}]`
   - `[:auth/login-success user-obj]`
   - `[:auth/login-error error-obj]`

- `[:message/send-to to-user-id content]`
  - `[:message/send-to-success message]`
  - `[:message/send-to-fail error]`
  

#### Requires refactoring. 

Currently support only one page and are clumsy to use.

- `[:inbox/fetch]` get users discussion threads form REST API
  - `[:inbox/fetch-success data]`
  - `[:inbox/fetch-failed error-obj]`
- `[:conversation/fetch user-id]` get conversion with given username
  - `[:conversation/fetch-success data]` 
  - `[:conversation/fetch-fail]`
- `[:show/conversation-with user-id]` Fetch discusison, move to conversation page and set current conversation (that should be shown to given user id).

#### Obsolete handlers 
- `[:load-db]` load whole db form local storage.
- `[:save-db]` save whole db to local storage.
- `[:set-db]` Helper for setting whole database.
- `[:storage-error]` error in loading or saving data to local storage.
- `[:logout]` Should be :auth/logout
- `[:login user-pwd-obj]` us :auth/login instead.

#### Handler ideas

- `[:sync/from-remote timestamp]` get all users, conversations related current user. This handler replaces `:inbox and :conversation handlers
  - `[:sync/from-remote-success data]`
  - `[:sync/from-remote-failed data]`  
- `[:sync/from-local]` load data from local storage. This is run always when user starts the app. 
  -  `[:sync/from-local-success data]`
  -  `[:sync/from-local-fail error-data]` (possibly user has never logged in)
- `[:sync/to-local]` save users, last-sync-timestamp and inbox to local storage.
  - `[:sync/to-local-success]`
  - `[:sync/to-local-fail]`

#### Other notes

* In new version of re-frame events with and without side-effects are separated more in more elegent way. This should be upgraded at somepoint to never version.

### Subscriptions

- `[:get-db]` returns db as is. This is mainly for testing in REPL, don't use it in any component.
- `[:inbox/get]` Returns user conversation listing.
- `[:get-page]` Get currently active page.
- `[:current-conversation]` Return currently active conversation (or nil)
- `[:get-user-of-current-converation]` Get user id of person with whom user have conversation with.
- `[:auth-status]` Return object that informs if authentication is in progress and succeed and possible error, if authentication have failed.

#### Notes

Naming should be consistent with handlers. E.g. :current-conversation should be :conversation/current. 

## License

[MIT](LICENSE)
