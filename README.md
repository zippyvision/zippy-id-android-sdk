ZippyId SDK written in Kotlin and Java.

## Requirements

- Kotlin version 1.3.0

## ZippyID API

Documentation can be found at https://docs.zippyid.com

### Dependencies

To get this library add the following line in your project's build.gradle:

`maven { url ‘https://jitpack.io’ }`

```Kotlin
allprojects {
   repositories {
        jcenter()
        maven { url "https://jitpack.io" }
   }
}
```

and in your app's build gradle add the dependency

`implementation 'com.github.mak-it:zippyid-sdk-android:Tag'`

```
dependencies {
   implementation 'com.github.mak-it:zippyid-sdk-android:Tag'
}
```

### Initialization

Before using the SDK, you must initialize it by calling 

```Kotlin
package com.zippyid.zippydroid

import android.app.Application
import com.zippyid.zippydroid.Zippy

class ZippyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Zippy.initialize(
            key: key,
            secret: secret
        )
    }
}
```

You can access the `key` and `secret` variables by going to [ZippyID admin panel](https://demo.zippyid.com/#/api_integrations) and creating a new API integration

### Usage

To start using, just create a new ZippyActivity class using the Zippydroid package

```Kotlin
package com.zippyid.zippydroid
import com.zippyid.zippydroid.ZippyActivity

...

val intent = Intent(packageContext: this, class: ZippyActivity::class.java)
startActivityForResult(intent, ZIPPY_RESULT_CODE)
```

### Results

To receive the user's results, just call the method `onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)`. Your desired data is stored in as an Intent.

```Kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ZIPPY_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getParcelableExtra<ZippyResponse>(ZippyActivity.ZIPPY_RESULT)
            } else {
                val error = data?.getStringExtra(ZippyActivity.ZIPPY_RESULT)
            }
        }
    }
}
```

If the session was successful `resultCode` is -1 (Activity.RESULT_OK) and `result` contains the sessions result.

If there was an error `resultCode` is 0 (Activity.RESULT_CANCELLED) and `error` contains the error message.
