## Debugging
To debug jar loading make sure you ... \
\- opened your project and JNativeLibLoader source code in Netbeans. \
\- built the current version of JNativeLibLoader's source code. \
\- deactivated “compile on save” in Netbeans.

To deactivate “compile on save”: \
Rightclick the project name -> Properties -> Build -> Compiling. Select "Disable". \
Do this for JNativeLibLoader' and your own project.

If “compile on save” is activated (which is the default case), Netbeans shows following hint in Run Output:
> Running NetBeans Compile On Save execution. Phase execution is skipped
> and output directories of dependency projects (with Compile on Save
> turned on) will be used instead of their jar artifacts.

That will also sometimes prevent the following error:
> Exception in thread "main" java.lang.UnsatisfiedLinkError: Can't load
> library:
> <**Project path**>/natives/linux-amd64/<*native lib name*>

**Why?** \
Netbeans caches the generated .class file before they are copied into the JAR. \
NativeLibLoader fetches the path of the .class file of “MyClass” from the JVM within the load() function. \
If “compile on save is activated” ... \
\- Netbeans loads the .class files of it's cache instead of the .class files from the JAR file when you run or debug your project. \
\- NativeLibLoader will then only get the path of the .class file in the Netbeans cache. \
NativeLibLoader can sometimes also load the native libs in the project path. But the aim was to debug the JAR loading.


## Possible inspirational projects for new functionality and refactoring
https://github.com/KeepSafe/ReLinker \
https://github.com/scijava/native-lib-loader \
https://github.com/LWJGL/lwjgl3


## NativeParsing
For more information [click here](NATIVE_PARSING.md).

An Alternative to NativeParsing which would also allow cyclic dependencies: \
Implement native lib loading itself via JNI to the OS specific functions. \
Linux: dlopen(). Would also allow lazy binding. \
Windows: LoadLibraryExA(). Allows to set dependency search path explicitly.

**Possible inspiration** \
https://javadoc.lwjgl.org/org/lwjgl/system/Library.html#loadNative(java.lang.Class,java.lang.String,java.lang.String) \
https://javadoc.lwjgl.org/org/lwjgl/system/windows/WinBase.html \
https://github.com/jnr/jnr-ffi

