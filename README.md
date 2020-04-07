# JavaCriuJar
Build a jar file for easier access to CRIU from Java

There's a makefile for building the jar.

This creates a libCheckpointRestore.so in your /tmp directory.

Caveats:
   You must rm /var/lib/sss/pipes/nss because that prevents criu from working right now.
   You must run with java -cp .:./checkpoint.jar -XX:+UseSerialGC -XX:-UsePerfData
