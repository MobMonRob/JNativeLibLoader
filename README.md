# JNativeLibLoader

Getestet unter Netbeans 12, Maven, Java SDK 11

## Dokumentation für Nutzer
### Aufrufendes Projekt konfigurieren
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


#### .so Dateien laden
Beispiel

    package de.dhbw.rahmlab.vicon.datastream.nativelib;
    
    import de.dhbw.rahmlab.vicon.datastream.impl.ViconDataStreamSDKSwigJNI;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * @author fabian
     */
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
    
    /*
    //Verwendung in Klassen, die hiervon abhängen:
        static {
            NativeLibLoader.load();
        }
     */

### Fehlerbehebung
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
