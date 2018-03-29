Trembita
========

This tiny library base on annotation processing and ```kotlin-poet``` can help you to write Kotlin-style interfaces and make your
code more readable. So lets check which problem it solves:

I'm sure you such interfaces in your code:

```kotlin
interface Callback {
    fun success(response: String, code: Int)
    fun error(errorCode: Int)
}
```
Then if you need to use it, probably you have something like this:
```kotlin
someClass.setCallback(object : Callback {
     override fun success(response: String, code: Int) {
     }
     override fun error(errorCode: Int) {
     }
})
```
With `@Trembita` you could write more attractive interfaces. Here is an example:
First that you need, just add `@Trembita` annotation to your interface
```kotlin
@Trembita
interface Callback {
    fun success(response: String, code: Int)
    fun error(errorCode: Int)
}
```
Then rebuild a project, and add method for interface initialization to class where you are going to use
interface instance:
```kotlin
class SomeClass {

    lateinit var callBack: Callback

    fun setListener(init: TrembitaCallback.() -> Unit) {
       callBack = TrembitaCallback().apply(init)
    }

    fun responseReceived() {
        callBack.success("response", 200)
     }
 }
```
So now your interface implementation will look like this:
```kotlin
someClass.setListener {
     success { response, code -> processResponse(response, code) }
     error { _ -> }
}
```
Now you can use lambdas and the whole Kotlin power. One more important thing
is that all methods are optional

**Problems that would be fixed in following releases**

- You should rebuild the project after adding `@Trembita` annotation
- Library supports only methods that return `Unit`
- Library does not support properties in interfaces

