# LibAndroidLog

Android Log into file and log in monitor.

## Getting started

In your `build.gradle`:

```gradle
 dependencies {
   compile 'com.hianzuo.android:LibAndroidLogger:1.0.0'
 }
```

In your `Application` class:

```java
public class ExampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    LogServiceHelper.init(this, "LOG_PATH", "LOG_FILE_PREFIX");
  }
}
```

**You're good to go!**

com.hianzuo.logger.Log.d(TAG,"message");
Will be log to your file and log in monitor now.

Questions? Check out [the FAQ](https://github.com/hianzuo/andorid-logger/wiki/FAQ)!

## License

    Copyright 2015 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
