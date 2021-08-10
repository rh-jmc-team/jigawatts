#!/bin/sh

#  Copyright 2021 Red Hat, Inc.
#
#  This file is part of Jigawatt.
#
#  Jigawatt is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 2, or (at your
#  option) any later version.
#
#  Jigawatt is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with Jigawatt; see the file COPYING.  If not see
#  <http://www.gnu.org/licenses/>.
#
#  Linking this library statically or dynamically with other modules
#  is making a combined work based on this library.  Thus, the terms
#  and conditions of the GNU General Public License cover the whole
#  combination.
#
#  As a special exception, the copyright holders of this library give you
#  permission to link this library with independent modules to produce an
#  executable, regardless of the license terms of these independent
#  modules, and to copy and distribute the resulting executable under
#  terms of your choice, provided that you also meet, for each linked
#  independent module, the terms and conditions of the license of that
#  module.  An independent module is a module which is not derived from
#  or based on this library.  If you modify this library, you may extend
#  this exception to your version of the library, but you are not
#  obligated to do so.  If you do not wish to do so, delete this
#  exception statement from your version.

# Test for autoconf commands.

# Test for autoconf.

HAVE_AUTOCONF=false

for AUTOCONF in autoconf autoconf259; do
    if ${AUTOCONF} --version > /dev/null 2>&1; then
        AUTOCONF_VERSION=`${AUTOCONF} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOCONF_VERSION}
        case ${AUTOCONF_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTOCONF=true
                break;
                ;;
        esac
    fi
done

# Test for autoheader.

HAVE_AUTOHEADER=false

for AUTOHEADER in autoheader autoheader259; do
    if ${AUTOHEADER} --version > /dev/null 2>&1; then
        AUTOHEADER_VERSION=`${AUTOHEADER} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOHEADER_VERSION}
        case ${AUTOHEADER_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTOHEADER=true
                break;
                ;;
        esac
    fi
done

# Test for autoreconf.

HAVE_AUTORECONF=false

for AUTORECONF in autoreconf; do
    if ${AUTORECONF} --version > /dev/null 2>&1; then
        AUTORECONF_VERSION=`${AUTORECONF} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTORECONF_VERSION}
        case ${AUTORECONF_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTORECONF=true
                break;
                ;;
        esac
    fi
done

if test ${HAVE_AUTOCONF} = false; then
    echo "No proper autoconf was found."
    echo "You must have autoconf 2.59 or later installed."
    exit 1
fi

if test ${HAVE_AUTOHEADER} = false; then
    echo "No proper autoheader was found."
    echo "You must have autoconf 2.59 or later installed."
    exit 1
fi

if test ${HAVE_AUTORECONF} = false; then
    echo "No proper autoreconf was found."
    echo "You must have autoconf 2.59 or later installed."
    exit 1
fi


# Test for automake commands.

# Test for aclocal.

HAVE_ACLOCAL=false

for ACLOCAL in aclocal aclocal-1.10; do
    if ${ACLOCAL} --version > /dev/null 2>&1; then
        ACLOCAL_VERSION=`${ACLOCAL} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${ACLOCAL_VERSION}
        case ${ACLOCAL_VERSION} in
            1.9.[6-9] | 1.1[0-9]* )
                HAVE_ACLOCAL=true
                break;
                ;;
        esac
    fi
done

# Test for automake.

HAVE_AUTOMAKE=false

for AUTOMAKE in automake automake-1.10; do
    if ${AUTOMAKE} --version > /dev/null 2>&1; then
        AUTOMAKE_VERSION=`${AUTOMAKE} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOMAKE_VERSION}
        case ${AUTOMAKE_VERSION} in
            1.9.[6-9] | 1.1[0-9]* )
                HAVE_AUTOMAKE=true
                break;
                ;;
        esac
    fi
done

if test ${HAVE_ACLOCAL} = false; then
    echo "No proper aclocal was found."
    echo "You must have automake 1.9.6 or later installed."
    exit 1
fi

if test ${HAVE_AUTOMAKE} = false; then
    echo "No proper automake was found."
    echo "You must have automake 1.9.6 or later installed."
    exit 1
fi


export ACLOCAL AUTOCONF AUTOHEADER AUTOMAKE

${AUTORECONF} --force --install
