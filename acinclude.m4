dnl Copyright 2021 Red Hat, Inc.
dnl
dnl This file is part of Jigawatt.
dnl
dnl Jigawatt is free software; you can redistribute it and/or modify
dnl it under the terms of the GNU General Public License as published
dnl by the Free Software Foundation; either version 2, or (at your
dnl option) any later version.
dnl
dnl Jigawatt is distributed in the hope that it will be useful, but
dnl WITHOUT ANY WARRANTY; without even the implied warranty of
dnl MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
dnl General Public License for more details.
dnl
dnl You should have received a copy of the GNU General Public License
dnl along with Jigawatt; see the file COPYING.  If not see
dnl <http://www.gnu.org/licenses/>.
dnl
dnl Linking this library statically or dynamically with other modules
dnl is making a combined work based on this library.  Thus, the terms
dnl and conditions of the GNU General Public License cover the whole
dnl combination.
dnl
dnl As a special exception, the copyright holders of this library give you
dnl permission to link this library with independent modules to produce an
dnl executable, regardless of the license terms of these independent
dnl modules, and to copy and distribute the resulting executable under
dnl terms of your choice, provided that you also meet, for each linked
dnl independent module, the terms and conditions of the license of that
dnl module.  An independent module is a module which is not derived from
dnl or based on this library.  If you modify this library, you may extend
dnl this exception to your version of the library, but you are not
dnl obligated to do so.  If you do not wish to do so, delete this
dnl exception statement from your version.

dnl Utility macros for jigawatts build

AC_DEFUN_ONCE([JW_CHECK_FOR_JDK],
[
  AC_MSG_CHECKING([for a JDK home directory])
  AC_ARG_WITH([jdk-home],
	      [AS_HELP_STRING([--with-jdk-home[[=PATH]]],
                              [jdk home directory (default is first predefined JDK found)])],
              [
                if test "x${withval}" = xyes
                then
                  SYSTEM_JDK_DIR=
                elif test "x${withval}" = xno
                then
	          SYSTEM_JDK_DIR=
	        else
                  SYSTEM_JDK_DIR=${withval}
                fi
              ],
              [
	        SYSTEM_JDK_DIR=
              ])
  if test -z "${SYSTEM_JDK_DIR}"; then
    AC_MSG_RESULT([not specified])
    OPENJDK8_VMS="/usr/lib/jvm/icedtea-8 /usr/lib/jvm/java-1.8.0-openjdk
    		  /usr/lib/jvm/java-1.8.0-openjdk.${RPM_ARCH} /usr/lib64/jvm/java-1.8.0-openjdk
		  /usr/lib/jvm/java-1.8.0 /usr/lib/jvm/java-8-openjdk"
    for dir in /usr/lib/jvm/java-openjdk /usr/lib/jvm/openjdk /usr/lib/jvm/java-icedtea \
	       /etc/alternatives/java_sdk_openjdk ${OPENJDK8_VMS} ; do
       AC_MSG_CHECKING([for ${dir}]);
       if test -d $dir; then
         SYSTEM_JDK_DIR=$dir ;
	 AC_MSG_RESULT([found]) ;
	 break ;
       else
         AC_MSG_RESULT([not found]) ;
       fi
    done
  else
    AC_MSG_RESULT(${SYSTEM_JDK_DIR})
  fi
  if ! test -d "${SYSTEM_JDK_DIR}"; then
    AC_MSG_ERROR("A JDK home directory could not be found.")
  fi
  AC_SUBST(SYSTEM_JDK_DIR)
])

AC_DEFUN_ONCE([JW_CHECK_FOR_JDK_VERSION],
[
  AC_REQUIRE([JW_CHECK_FOR_JDK])
  AC_CACHE_CHECK([for the JDK version], jw_cv_jdk_version, [
  CLASS=Test.java
  BYTECODE=$(echo $CLASS|sed 's#\.java##')
  mkdir tmp.$$
  cd tmp.$$
  cat << \EOF > $CLASS
[/* [#]line __oline__ "configure" */

public class Test
{
    public static void main(String[] args)
    {
      System.out.println(System.getProperty("java.specification.version"));
    }
}]
EOF
  if ${SYSTEM_JDK_DIR}/bin/javac -cp . $JAVACFLAGS -source 8 -target 8 $CLASS >&AS_MESSAGE_LOG_FD 2>&1; then
    if ${SYSTEM_JDK_DIR}/bin/java -classpath . $BYTECODE >&AS_MESSAGE_LOG_FD 2>&1; then
      JDK_VERSION=$(${SYSTEM_JDK_DIR}/bin/java -classpath . $BYTECODE);
      jw_cv_jdk_version=${JDK_VERSION};
    else
      jw_cv_jdk_version=no;
      AC_MSG_ERROR([VM failed to run compiled class.])
    fi
  else
    jw_cv_jdk_version=no;
    AC_MSG_ERROR([Compiler failed to compile Java code.])
  fi
  rm -f $CLASS *.class
  cd ..
  rmdir tmp.$$
  ])
AC_SUBST([JDK_VERSION])
AC_PROVIDE([$0])dnl
])
)

AC_DEFUN_ONCE([JW_FIND_TOOL],
[AC_PATH_TOOL([$1],[$2])
 if test x"$$1" = x ; then
   AC_MSG_ERROR([The following program was not found on the PATH: $2])
 fi
 AC_SUBST([$1])
])

AC_DEFUN_ONCE([JW_CHECK_FOR_RMDIR],
[
  JW_FIND_TOOL([RMDIR],[rmdir])
  AC_CACHE_CHECK([if ${RMDIR} supports --ignore-fail-on-non-empty], jw_cv_RMDIR, [
    mkdir tmp.$$
    touch tmp.$$/t
    if ${RMDIR} --ignore-fail-on-non-empty tmp.$$ >&AS_MESSAGE_LOG_FD 2>&1; then
       jw_cv_RMDIR=yes;
       RMDIR="${RMDIR} --ignore-fail-on-non-empty"
    else
       jw_cv_RMDIR=no;
    fi
  ])
  rm -f tmp.$$/t
  ${RMDIR} tmp.$$
])
