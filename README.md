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

1. In re-frame application state is stored in an ratom called db (database). Ratom (reactive atom) is a observable datastructure that ensure atomic modifications (i.e. only one change at time to the latest version of data it contains). The sturucture of db is constraint by schema. Currently prismatic schema is used, but it will change to core.spec at somepoint.
2. Database is modified by events. The logic how an event changes db is specifed by a handler.
3. UI component listens re-frame subscription. Each subscription is a observable view to db. You need to specify view to data in subs.cljs first, then you can subscribe it in UI component by using `re-frame.core/subscribe` function that reaturn ratom.
4. UI component will dispatch events to re-frame dispatch. The events is handled by handler in ordre they are arrived to the dispatcher on by one. If you dispatch an event in handler if will be handled *after* the event you are handling. (I.e. you cannot dispatch event syncronously in an handler.) Each handler must reaturn an updated version of db, and each of them get curent db as parameter. After a handler returns, subscribtions triggers.

In most cases you need to do you need to answer three question when you develop new feature:

1. What data feature needs? 
  #. You may need to extend schema and db. 
  #. You need to write a subscrition or maybe two. UI components should not depend directly to db-structure.
2. How data changes?
  #. Implement a handler for each reason to change.
  #. Re-frame anticipate thinking style where you just thing how the data changes, i.e. what are the situations. E.g. if data should be change by a REST call, you probably need  handler for successfull call and unsuccessful call. That where you start. Then you probably add handler that does the REST call and changes db so that user knows that something is happening. Handler for successful call, and errorous call does not know where the data related to an event comes from. Handler that does the call, don't know how the response is used. Pros of this approche is that you can easily change any of the parts without modifying other parts (open/closed principle) and easily trigger any change from REPL or in tests; cons for this this approach is that it can be hard to understand how the data flows and where it comes.
3. When data should be fetched? There are three options: 
  #. When app starts. In this case you dispatch fetch event in [platform]/core.clsj init function. There is separate core.clsj for Androind and iOS in. 
  #. When used does something. In this case you dispatch the event from react native UI component event handler.
  #. Periodically (e.g. once per 1 minute). You can use JavaScript setTimeout and put it to handler.cljs file. Currentlty this is a bit clumsy and problably you should create helper function for this. 
4. How user interact with data (UI)? 
  #. The goal is that there is as much shared components as possible. UI components are located in shared, andoid and iOS directories. In addition to documentation there is now way to know what component works in which framework. Not all components supports all features in React Native component and component may behave differently on differnt platforms. This is React Native design desision.
5. What component should looklike?
  #. Till now we have used a lot of inline style. They should be refactored to platform specific styles.cljs file.

### Project structure

Actual source code you should modify are in ./src/trustroots/ and ./test/ directories, and images are in /images/ directory. In addition to those there are two files you need to modify:

- package.json contains npm packages.
- .re-natal is used by leiningen to bundle JavaScript source for react-native. You need to list there all npm packages that should be included in react native app. Leiningen does not use package.json for this. I.e. if you want to use an 3rd party library you need install it first by using npm and then add the name of packages to this file. This file contains also some other build related information, and it is modified e.g. when you swap between emulator and physical machine.

#### src/trustroots structure:

- android: android specific UI components goes to this folder
- domain: contains shema specs.
- ios: iOS specific UI components goes to this folder. Currently app does not support iOS so there's not much here.
- shared: all component thar should work both in android and iOS are located here.
- api.cljs: REST api call helpers goes here.
- db.cljs: This file contains helpers for persisting data, including caching mechanims.
- fetch_helper.cljs contain wrapper for react native fetch function. This is used for REST calls.
- handlers.clsj contains all re-frame handels. Currently this file is rather long and it might be split to many files.
- subs.cljs contains observable re-frame subscription.

### App state (db)scheama 

Currently domain model reflects closely to what REST api's return. This need to be changed as the app should support off-line use and rest APIs may not return all data (they page data). 

#### Proposall for new schema

- users: map of all users curent use have had a converstion, or that are his friends. Key: Id, value: user-data
- me: tells who of the user list is me. Id only
- last-sync-time: last change timestamp (this should come form server)
- inbox: {
   *thread*: list of messages
}
- current-page: vector, e.g. [:discussion thread]
- errors: vector of errors
- services: {
   :toaster obj
   :featch obj
}

#### Other notes

* In future Prismatic schmema should be updated to core.spec.

### Handlers

- Inintialization 
  - `[:initialize-db]` loads intial version of database.
  - `[:registe-service service-obj]` Register helper function for dependency injection.
- Navigation
  - `[:set-page page]` Change UI view.

- Autentication related
  - `[auth/login {:user username :pwd password}]`
   - `[auth/login-success user-obj]`
   - `[auth/login-error error-obj]`

- `[:message/send-to to-user-id content]`
  - `[:message/send-to-success message]`
  - `[:message/send-to-fail error]`
  

Require refactoring. Currently support only one page and are clumsy to use.
- `[inbox/fetch]` get users discussion threads form REST API
  - `[inbox/fetch-success data]`
  - `[inbox/fetch-failed error-obj]`
- `[:conversation/fetch user-id]` get conversion with given username
  - `[:conversation/fetch-success data]` 
  - `[:conversation/fetch-fail]`
- `[show\conversation-with user-id]` Fetch discusison, move to conversation page and set current conversation (that should be shown to given user id).


Obsolete handlers 
- `[:load-db]` load whole db form local storage.
- `[:save-db]` save whole db to local storage.
- `[:set-db]` Helper for setting whole database.
- `[:storage-error]` error in loadin or saving data to local storage.
- `[:logout]` Should be :auth/logout
- `[:login user-pwd-obj]` us :auth/login instead.

#### Handler ideas

- `[sync/from-remote timestamp]` get all users, conversations related current user. This handls replaces :inbox and :converstion handlers
  - `[sync/from-remote-success data]`
  - `[sync/from-remote-failed data]`  
- `[sync/from-local]` load data from local storage. This is run allways when user starts app. 
  -  `[sync/from-local-success data]`
  -  `[sync/from-local-fail error-data]` (possibly uses have not logged ever)
- `[sync/to-local]`` save users, last-sync-timestam and inbox to local storage.
  - `[sync/to-local-success]`
  - `[sync/to-local-fail]`

#### Other notes

* In new version of re-frame events with and without side-effect are separated more in more elegent way. This should be upgraded at somepoint to never version.

### Subscriptions

- `[:get-db]` returns db as is. This is mainly for testing in REPL, don't use it in any component.
- `[:inbox/get]` Returns user convesation listing.
- `[:get-page]` Get currently active page.
- `[:current-conversation]` Return currently active coveration (or nil)
- `[:get-user-of-current-converation]` Get user id of person with whom user have converation with.
- `[:auth-status]` Return object that informs if authentication is in progress and succeed and possible error, if authentication have failed.

#### Notes

Namesing should be consistent with handler. E.g. :current-conversation should be :coversation/current. 

## License

[MIT](LICENSE)
