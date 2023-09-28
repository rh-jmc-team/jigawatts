#  Copyright 2021 Red Hat, Inc.
#
#  This file is part of Jigawatts.
#
#  Jigawatts is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 2, or (at your
#  option) any later version.
#
#  Jigawatts is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with Jigawatts; see the file COPYING.  If not see
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

JIGA_BUILDDIR = $(abs_top_builddir)/build
JIGA_STAMPDIR = $(abs_top_builddir)/stamps
JIGA_INCLUDEDIR = $(JIGA_BUILDDIR)/include
JIGA_SRCDIR = $(abs_top_srcdir)/src/main
JIGA_NATIVE_SRCDIR = $(JIGA_SRCDIR)/cpp
JIGA_JAVA_SRCDIR = $(JIGA_SRCDIR)/java
JIGA_MAVEN_DIR = META-INF/maven/$(GROUP)/$(ARTIFACT)

JIGA_NATIVE_FUNCS = com/redhat/jigawatts/Jigawatts.java
JIGA_NATIVE_HDRS = $(subst .java,.h,$(subst /,_,$(JIGA_NATIVE_FUNCS)))
JIGA_NATIVE_SRC = $(subst .java,.cpp,$(subst /,_,$(JIGA_NATIVE_FUNCS)))
JIGA_NATIVE_OBJS = $(subst .cpp,.o,$(JIGA_NATIVE_SRC))
JIGA_LIB = libJigawatts.so
JIGA_JAR = $(ARTIFACT).jar
JAVA_ROOT_DIR = $(word 1, $(subst /, ,$(JIGA_NATIVE_FUNCS)))

LICENSE_DIR = $(datadir)/licenses/$(PACKAGE)
JAVADOC_DIR = $(datadir)/javadoc/$(PACKAGE)
JAR_DIR = $(datadir)/java/$(PACKAGE)

EXTRA_DIST = src README.md LICENSE.md pom.xml

JAVADOCFLAGS = -private -use -version -Xdoclint:none -encoding 'UTF-8' -charset 'UTF-8' -docencoding 'UTF-8'
JAVADOC_ARGS = $(JAVADOCFLAGS) -d $(JIGA_BUILDDIR)/doc -sourcepath $(JIGA_JAVA_SRCDIR) \
	  -classpath $(JIGA_BUILDDIR) -doctitle 'Javadoc for package $(PACKAGE)'

all-local: $(JIGA_BUILDDIR)/$(JIGA_JAR) $(JIGA_BUILDDIR)/$(JIGA_LIB) $(JIGA_STAMPDIR)/docs.stamp

.PHONY: clean-lib clean-native-objects clean-classes-and-headers clean-jar clean-docs clean-source-file-list

clean-local: clean-lib clean-native-objects clean-classes-and-headers clean-jar clean-docs clean-source-file-list
	$(RMDIR) build stamps

$(JIGA_BUILDDIR)/source-files.txt:
	mkdir -p $(JIGA_BUILDDIR)
	find $(JIGA_JAVA_SRCDIR) -name '*.java' | sort > $@ ;
	touch $@

clean-source-file-list:
	$(RM) $(JIGA_BUILDDIR)/source-files.txt

$(JIGA_STAMPDIR)/classes.stamp: $(JIGA_BUILDDIR)/source-files.txt
	$(SYSTEM_JDK_DIR)/bin/javac -g $(JAVACFLAGS) \
	      -d $(JIGA_BUILDDIR) -h $(JIGA_INCLUDEDIR) \
	      -sourcepath $(JIGA_JAVA_SRCDIR) \
	      @$< ;
	mkdir -p $(JIGA_STAMPDIR)
	touch $@

clean-classes-and-headers:
	$(RM) $(addprefix $(JIGA_INCLUDEDIR)/,$(JIGA_NATIVE_HDRS))
	$(RM) -r $(JIGA_BUILDDIR)/$(JAVA_ROOT_DIR)
	$(RM) $(JIGA_STAMPDIR)/classes.stamp

$(JIGA_BUILDDIR)/%.o: $(JIGA_NATIVE_SRCDIR)/%.cpp
	$(CC) $(CFLAGS) -I$(SYSTEM_JDK_DIR)/include \
	  -I$(SYSTEM_JDK_DIR)/include/linux -I$(JIGA_INCLUDEDIR) $(CRIU_CFLAGS) \
	  -fPIC -o $@ -c $<

clean-native-objects:
	$(RM) $(addprefix $(JIGA_BUILDDIR)/,$(JIGA_NATIVE_OBJS))

$(JIGA_BUILDDIR)/$(JIGA_LIB): $(addprefix $(JIGA_INCLUDEDIR)/,$(JIGA_NATIVE_HDRS)) \
  $(addprefix $(JIGA_BUILDDIR)/,$(JIGA_NATIVE_OBJS))
	$(CC) $(LDFLAGS) $(addprefix $(JIGA_BUILDDIR)/,$(JIGA_NATIVE_OBJS)) -shared -o $@ $(CRIU_LIBS) $(CFLAGS)

clean-lib:
	$(RM) $(JIGA_BUILDDIR)/$(JIGA_LIB)

$(JIGA_BUILDDIR)/$(JIGA_JAR): $(JIGA_STAMPDIR)/classes.stamp
	cd $(JIGA_BUILDDIR) && \
	  $(MKDIR_P) $(JIGA_MAVEN_DIR) && \
	  cp $(abs_top_srcdir)/pom.xml $(JIGA_MAVEN_DIR) && \
	  cp $(abs_top_builddir)/autotools.pom.properties $(JIGA_MAVEN_DIR)/pom.properties && \
	  $(SYSTEM_JDK_DIR)/bin/jar -cvfm $(JIGA_JAR) $(abs_top_builddir)/MANIFEST.MF ./$(JAVA_ROOT_DIR) META-INF

clean-jar:
	$(RM) $(JIGA_BUILDDIR)/$(JIGA_JAR)
	$(RM) -r $(JIGA_BUILDDIR)/$(JIGA_MAVEN_DIR)

$(JIGA_STAMPDIR)/docs.stamp: $(JIGA_BUILDDIR)/source-files.txt
	$(SYSTEM_JDK_DIR)/bin/javadoc $(JAVADOC_ARGS) @$< ;
	for word in $(JAVADOC_ARGS); do echo $${word} >> $(JIGA_BUILDDIR)/doc/args; done
	mkdir -p $(JIGA_STAMPDIR)
	touch $@

clean-docs:
	$(RM) -r $(JIGA_BUILDDIR)/doc
	$(RM) $(JIGA_STAMPDIR)/docs.stamp

install-exec-local:
	${mkinstalldirs} $(DESTDIR)$(JAR_DIR) $(DESTDIR)/$(libdir)
	$(INSTALL_DATA) $(JIGA_BUILDDIR)/$(JIGA_JAR) $(DESTDIR)$(JAR_DIR)
	$(INSTALL_PROGRAM) $(JIGA_BUILDDIR)/$(JIGA_LIB) $(DESTDIR)$(libdir)

install-data-local:
	$(mkinstalldirs) $(DESTDIR)$(LICENSE_DIR) $(DESTDIR)$(docdir)
	$(mkinstalldirs) $(DESTDIR)$(JAVADOC_DIR)
	$(INSTALL_DATA) $(abs_top_srcdir)/README.md $(DESTDIR)$(docdir)
	$(INSTALL_DATA) $(abs_top_srcdir)/LICENSE.md $(DESTDIR)$(LICENSE_DIR)
	cp -RP $(JIGA_BUILDDIR)/doc/* $(DESTDIR)$(JAVADOC_DIR)

uninstall-local:
	$(RM) -v $(DESTDIR)$(JAR_DIR)/$(JIGA_JAR)
	$(RMDIR) $(DESTDIR)$(JAR_DIR)
	$(RM) -v $(DESTDIR)$(libdir)/$(JIGA_LIB)
	$(RM) -v $(DESTDIR)$(docdir)/README.md
	$(RMDIR) $(DESTDIR)$(docdir)
	$(RM) -v $(DESTDIR)$(LICENSE_DIR)/LICENSE.md
	$(RMDIR) $(DESTDIR)$(LICENSE_DIR)
	$(RM) -rv $(DESTDIR)$(JAVADOC_DIR)
