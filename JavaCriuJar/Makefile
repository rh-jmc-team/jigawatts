DIR = org/checkpoint

clean:
	$(RM) $(DIR)/*.class *.hpp *.so

classes:
	javac $(DIR)/*.java

hs:
	javac -h . org/checkpoint/CheckpointRestore.java


hpps: 	hs
	mv org_checkpoint_CheckpointRestore.h org_checkpoint_CheckpointRestore.hpp	

libs:	hpps
	gcc -v -shared -fPIC  -I/usr/lib/jvm/java-openjdk/include -I/usr/lib/jvm/java-openjdk/include/linux -I/usr/lib64/  -I/usr/include/criu -lcriu -o ./libCheckpointRestore.so org_checkpoint_CheckpointRestore.cpp

jar:     classes libs
	jar -cvfM checkpoint.jar ./org libCheckpointRestore.so

