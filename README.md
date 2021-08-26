# jigawatts

Build a jar file for easier access to CRIU from Java

Dependencies:
You must have java-devel and criu-devel installed on your system.
$JAVA_HOME/include/jni.h must point to a valid jni.h file.

Caveats:
   You must rm /var/lib/sss/pipes/nss because that prevents criu from working right now.
   You must run with java -cp .:./checkpoint.jar -XX:+UseSerialGC -XX:-UsePerfData
   
   
   You must run tests as root.  CRIU will run in user mode eventually, but not everywhere yet.

### Recognized java properties/system variables
If jigawatts is packed with embedded dynamic library, it is used in advance. If it is missing, the system library is searched for.
To change the loading of native bits you can use following properties/variables. Property is used with priority.
* The internal library will be unpacked and loaded from given file. If given file exists, it is not overwritten
  `-Djigawatts.library.targetfile/$JIGAWATTS_LIBRARY_TARGETFILE`=file
* The internal library will not be used, given file will be used
  `-Djigawatts.library/$JIGAWATTS_LIBRARY`=file/SYSTEM
  You may use value of `SYSTEM` to force search of system library even with internal library present
* This switch can set library loading logging to 'true'
  `-Djigawatts.verbose.file/$JIGAWATTS_VERBOSE_FILE`=file
* This switch can set library loading logging to append to exact file instead of stderr
  `-Djigawatts.verbose/$JIGAWATTS_VERBOSE`=true/false


## Building with autotools

The source directory can be obtained either from a release tarball
or a checkout of the git source code repository.  When using the
git repository, it is necessary to install the automake and autoconf
tools and run `autogen.sh` to create the configure script.

```
$ mkdir <build_dir>
$ <src_dir>/configure --prefix=<install prefix>
$ make
$ make install
```

## Building with Maven

```
 $ mvn clean install
```
Is usually enough, but on some systems you need to go with root
```
 $ sudo mvn clean verify
```

