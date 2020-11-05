#include "org_checkpoint_CheckpointRestore.hpp"
#include <criu.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <jni.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/un.h>
#include <sys/wait.h>
#include <unistd.h>

/*
 * Class:     org_checkpoint_CheckpointRestore
 * Method:    CheckTheWorldNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_checkpoint_CheckpointRestore_checkTheWorldNative
(JNIEnv *env, jobject obj)  {

    int init_result = criu_init_opts();

    if (init_result < 0) {
        perror("Can't init opts");
    }

    int result = criu_check();

    if (result == 0) {
        printf("Criu Check success\n");
    } else if (result == -52) {
        printf("Criu got an error, but should still work, %d: %s\n", result, strerror(result));
    } else {
        printf("Criu Check failed with error: %d: %s\n", result, strerror(result));
    }
    return;
}
  
/*
 * Class:     org_checkpoint_CheckpointRestore
 * Method:    SaveTheWorldNative
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_checkpoint_CheckpointRestore_saveTheWorldNative (JNIEnv * env, jobject jobj, jstring jstr) {
    const char * path = env->GetStringUTFChars(jstr, NULL);
    struct stat st = {0};

    if (stat(path, &st) == -1) {
      mkdir(path, 0700);
    }

    int fd = open(path, O_DIRECTORY);

    printf("\npath = %s\n",path);

    if (fd < 0) {
      perror("Can't open images dir");
    }

    int init_result = criu_init_opts();

    if (init_result < 0) {
      perror("Can't init opts");
    }

    criu_set_images_dir_fd(fd);
    criu_set_shell_job(true);
    criu_set_log_level(4);  

    criu_set_log_file((char *) "save.log");
    criu_set_leave_running(true);


    criu_set_ext_unix_sk(true);
    int ret = criu_dump();

    if (ret >= 0) {
      printf("Successful dump\n");
    } else {
      printf("Error from dump %d\n", ret);
      perror("Dump Error");
    }
}

/*
 * Class:     org_checkpoint_CheckpointRestore
 * Method:    RestoreTheWorldNative
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_checkpoint_CheckpointRestore_restoreTheWorldNative
(JNIEnv * env, jobject jobj, jstring jstr) {
    const char * path = env->GetStringUTFChars(jstr, NULL);  
    int fd = open(path, O_DIRECTORY);

    if (fd < 0) {
        perror("Can't open images dir");
    }

    int init_result = criu_init_opts();

    if (init_result < 0) {
        perror("Can't init opts");
    }

    printf("RestoreTheWorld: file = %s fd = %d\n", path, fd);

    criu_set_shell_job(true);
    criu_set_images_dir_fd(fd);
    criu_set_log_file((char *) "javarestore.log");
    criu_set_log_level(4);

    int pid = criu_restore_child();

    if (pid < 0) {
        perror("Criu Restore Bad Pid \n");
    } else {
        int status = 0;
        int result = waitpid(pid, &status, 0);
        if (result < 0) {
            printf("pid = %d status = %d result = %d\n", pid, status, result);    
            perror("Can't wait rchild");
        }
    }
}

/*
 * Class:     org_checkpoint_CheckpointRestore
 * Method:    MigrateTheWorld
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_checkpoint_CheckpointRestore_migrateTheWorld
  (JNIEnv *, jclass);

/*
 * Class:     org_checkpoint_CheckpointRestore
 * Method:    SaveTheWorldIncremental
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_checkpoint_CheckpointRestore_saveTheWorldIncremental
  (JNIEnv *, jclass);

