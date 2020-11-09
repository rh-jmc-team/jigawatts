# JavaCriuJar

Build a jar file for easier access to CRIU from Java

Dependencies:
You must have java-devel and criu-devel installed on your system.
$JAVA_HOME/include/jni.h must point to a valid jni.h file.

Caveats:
   You must rm /var/lib/sss/pipes/nss because that prevents criu from working right now.
   You must run with java -cp .:./checkpoint.jar -XX:+UseSerialGC -XX:-UsePerfData
   
   
   You must run tests as root.  CRIU will run in user mode eventually, but not everywhere yet.

## Building with Maven (experimental)

```
 $ cd JavaCriuJar
 $ mvn clean verify


