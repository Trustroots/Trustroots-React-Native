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

## License

[MIT](LICENSE)
