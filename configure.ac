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

AC_INIT([jigawatts], [1.0-SNAPSHOT], [chf@redhat.com])
AC_CANONICAL_HOST
AC_CANONICAL_TARGET
AM_INIT_AUTOMAKE([1.9 tar-pax foreign])
AM_MAINTAINER_MODE([enable])
AC_CONFIG_FILES([Makefile pom.properties MANIFEST.MF])

AC_PROG_CC

JW_CHECK_FOR_JDK_VERSION
JW_CHECK_FOR_RMDIR

dnl Check for CRIU
PKG_CHECK_MODULES(CRIU,criu,[CRIU_FOUND=yes],[CRIU_FOUND=no])
if test "x${CRIU_FOUND}" = xno
then
  AC_MSG_ERROR([Could not find CRIU headers - Try installing criu-devel.])
fi
AC_SUBST(CRIU_CFLAGS)
AC_SUBST(CRIU_LIBS)

GROUP=org.openjdk
ARTIFACT=jigawatts
AC_SUBST(GROUP)
AC_SUBST(ARTIFACT)

AC_OUTPUT
