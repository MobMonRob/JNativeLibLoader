# JNativeLibLoader
## Description
The aim of JNativeLibLoader is to offer a mighty and easy to use library to load native libraries from JAR files. \
This includes:
1. unpacking binary native library files from the JAR to a cache
2. recognizing the platform the using application is running at
3. loading the binary native library files into the jvm depending on the platform

## Credits
Most of the source code in this repository is a more ore less customized extraction from the Gluegen project of the Jogamp community from 15. September 2020. \
You can find the full source code of the Gluegen project here: https://github.com/JogAmp/gluegen. \
The copy of the Gluegen license is in the file `Gluegen_LICENSE.txt`. \
The following folders contain customized, extracted Gluegen code:
* `/NativeLibLoader/src/main/java/de/dhbw/rahmlab/nativelibloader/impl/com/jogamp/common`
* `/NativeLibLoader/src/main/java/de/dhbw/rahmlab/nativelibloader/impl/jogamp/common`
* `/NativeGenerator_Linux64/include`
* `/NativeGenerator_Linux64/src`

## Documentation for all
Tested with Netbeans 12, Maven, Java SDK 11, GCC 7.5.0

## Dokumentation für Nutzer
Beispiel für ein funktionierendes abhängiges Projekt: https://github.com/MobMonRob/JViconDataStream2

### Verwendendes Projekt konfigurieren
Das Repository dieses Projektes dem Repository des verwendenden Projektes als `git submodule` hinzufügen.

#### Dependency konfigurieren
In pom.xml hinzufügen:

        <dependency>
            <groupId>de.dhbw.rahmlab</groupId>
            <artifactId>NativeLibLoader</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

#### .so Dateien laden konfigurieren
Damit die .so Dateien geladen werdem können, müssen sie sich unter `<Projektpfad>/target/classes/natives/linux-amd64/` befinden. Damit landen sie innerhalb des JARs in `/natives/linux-amd64/`. Das Kopieren lässt sich mit Maven bewerkstelligen:

            <resource>
                <targetPath>${basedir}/target/classes/natives</targetPath>
                <directory>${basedir}/natives</directory>
            </resource>


#### .so Dateien laden vorbereiten
So eine ähnliche Klasse anlegen:

    package de.dhbw.rahmlab.vicon.datastream.nativelib;
    
    import de.dhbw.rahmlab.vicon.datastream.impl.ViconDataStreamSDKSwigJNI;
    import java.util.ArrayList;
    import java.util.List;
    
    public class NativeLibLoader {
    
        private static boolean isLoaded = false;
    
        public static void load() {
            if (!isLoaded) {
                loadActually();
                isLoaded = true;
            }
        }
    
        private static void loadActually() {
            List<String> glueLibNames = new ArrayList<>();
            glueLibNames.add("jViconDataStreamSDK");
    
            de.dhbw.rahmlab.nativelibloader.api.NativeLibLoader.load(glueLibNames, ViconDataStreamSDKSwigJNI.class);
        }
    }

#### .so Dateien laden
Innerhalb einer Klasse, die die Bibliothek benötigt. Am besten natürlich die Klasse, die die `native` Aufrufe definiert. Geht zur Not aber auch in einer diese aufrufenden Klasse.

        static {
            NativeLibLoader.load();
        }

#### Fehlerbehebung
Falls man das Quelltextprojekt benutzt und das Laden des JARs debuggen möchte:
In Netbeans "compile on save" deaktiveren. ->Siehe [StackOverflow Frage](https://web.archive.org/web/20201113173334/https://stackoverflow.com/questions/1304149/disabling-automatic-build-in-netbeans/1313691#1313691) \
Netbeans gibt sonst im Run Output folgenden Hinweis:
> Running NetBeans Compile On Save execution. Phase execution is skipped
> and output directories of dependency projects (with Compile on Save
> turned on) will be used instead of their jar artifacts.

Das verhindert folgenden Fehler:

> Exception in thread "main" java.lang.UnsatisfiedLinkError: Can't load
> library:
> `<path to project>`/`<project name>`/natives/linux-amd64/libgluegen_rt.so

Die Ursache ist, dass Netbeans standardmäßig ("compile on save" aktiviert) die .class Dateien einer Dependency lädt, sofern die Dependency ein Netbeans Projekt ist. Mit der Deaktivierung dieser Funktion wird die JAR benutzt. \
**Achtung: Man darauf achten, dass das JAR Artefakt des NativeLibLoader Projekts erstellt worden ist bevor man das aufrufende Projekt baut.**

## Dokumentation für Entwickler
#### Interessante ähnliche Projekte
https://github.com/KeepSafe/ReLinker \
https://github.com/scijava/native-lib-loader
