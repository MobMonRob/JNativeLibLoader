## Description
JNativeLibLoader' is a powerful library with a simple API to load native libraries (.dll, .so) from JAR files into the JVM for java applications which need to invoke native code via jni in a platform independend manner.

This includes:
- recognizing the platform the using application is running at.
- finding native libs based on a provided marker class only.
- unpacking binary native library files from the JAR to a cache.
- loading the binary native library files into the jvm depending on the platform.

The native libs bundled within the JAR will be loaded in the right sequence if they mutually depend on each other. But loading will fail if their dependency graph contains cycles.

More Developer Info [here](DEVELOPER_INFO.md).


## Limitation
At the moment only Linux x86-64 and Windows x86-64 are supported.


## Tested prerequisites
* Netbeans 23
* Java SDK 21
* Maven


## How to use it
Clone the repository and build the project. This will add JNativeLibLoader to the local Maven cache.
You only need to rebuild JNativeLibLoader if you change it's codebase.

Then follow the steps in the next section to configure your project.

Exemplary project: [JCasADi](https://github.com/MobMonRob/JCasADi).


## Configure your project
#### Add to Maven pom
If you are not familiar with Maven, search the internet how to properly include this snippet:

~~~xml
<dependency>
	<groupId>de.orat.rahmlab</groupId>
	<artifactId>NativeLibLoader</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
~~~


#### Add native files
Your project structure should look similar to this:

~~~
Myproject
|- src
   |- main
      |- java
      |- resources
         |- nativeLibs
            |- linux-amd64
               |- <.so files here>
            |- windows-amd64
               |- <.dll files here>
~~~


#### Load them in your code
Make sure that you load the native files before you invoke any jni code and that you load them only once.

Snippets:

~~~java
import de.dhbw.rahmlab.nativelibloader.api.NativeLibLoader;
~~~

~~~java
NativeLibLoader.loadLibs(MyClass.class);
~~~

Substitute `MyClass` with any class from your project. If you generate multiple JAR's make sure that `MyClass` is in the same JAR as the native files.


#### Big JAR
To make deployment easy it can be a good idea if you bundle your project code, your native libs and the JNativeLibLoader' functionality into one single JAR file.

A way to do this is to configure maven-assembly-plugin to build a jar-with-dependencies. \
You can find examples for this procedure e.g. here: https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven.

