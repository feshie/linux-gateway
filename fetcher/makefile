#############
# Settings
#############

# Directories containing source files (leave blank if current directory)
SRC:=src/ protocol-buffers/java/

# Directory compiled files will be stored in (executable will be copied to top level dir)
OBJDIR:=build/

# Additional libraries required - will be copied into JAR
LIBRARY:=include/

# Name of the created jar
OUTPUT_NAME:=fetcher.jar

# Main class - will be specified in the manifest
MAIN:=org.mountainsensing.fetcher.Main

# Command to get the Version/ Revision number of the JAR. Will be set to unknown if command returns != 0
# (--dirty will show when wordking dir is dirty, --always will always print a version, even if there is no tag)
VERSION_CMD:=git describe --long --tags --dirty --always

# Directory that will be used a base for the META-INF dir of the JAR
# All of it's contents will be copied over
SOURCE_MANIFEST_DIR:=META-INF/

#############
# Make options
#############

# Set the shell to be bash
SHELL:=/bin/bash

# Sane exit / error reporting options for bash
# errexit: exit bash with $? if $? != 0
# pipefail: a | b will exit with the first $? != 0 instead of the $? of b
export SHELLOPTS:=errexit:pipefail

# Delete target files on error
.DELETE_ON_ERROR:

# Disable built in suffix rules
.SUFFIXES:

# Do not echo commands
#.SILENT:

# Disable builtin pattern rules
MAKEFLAGS+=-r

# Set default target
.DEFAULT_GOAL:=build

#############
# Variables
#############

# An empty variable. Useful for getting special characters such as spaces and tabs.
BLANK:=

# A literal space.
SPACE:=$(BLANK) $(BLANK)

# Java commands
JAVA:=java
JAVAC:=javac
JAR:=jar

# Options passed to java commands
# Enable all warnings and treat warnings as errors
# Disable static warnings for the protocol buffers
JAVAC_OPTS+=-Xlint:all -Xlint:-static -Werror
JAR_OPTS+=cmf
JAVA_OPTS+=

# Shell commands
MKDIR:=mkdir -p
FIND:=find

# List of source files
SOURCE:=$(shell $(FIND) $(SRC) -name '*.java')

# List of JARS that will be extracted and included
LIBS_JAR:=$(wildcard $(LIBRARY)*.jar)

# List of directories that will be included
LIBS_DIR:=$(filter-out $(LIBS_JAR),$(wildcard $(LIBRARY)*))

# List of lib tags in the build directory
LIBS_TAG:=$(addprefix $(OBJDIR).tag_,$(notdir $(LIBS_JAR:.jar=) $(LIBS_DIR)))

# File used as a tag for directory creation
DIRTAG:=.dirtag

# File that holds a list of source files
SOURCE_FILE:=$(OBJDIR).files

# Tag for compilation dates of java source
COMPILE:=$(OBJDIR).compile

# Folder JAR will be made from
JARDIR:=$(OBJDIR)jar/

# Files contained within the Source Manifest Dir (excluding hidden files)
SOURCE_MANIFEST_FILES:=$(shell $(FIND) $(SOURCE_MANIFEST_DIR) -type f -not -name ".*")

# Directory for the JAR manifest file
MANIFEST_DIR:=$(JARDIR)META-INF/

# Manifest file for JAR
MANIFEST:=$(MANIFEST_DIR)MANIFEST.MF

# Version of the JAR
VERSION:=$(shell version=`$(VERSION_CMD) 2>/dev/null`; if [ $$? -eq 0 ]; then echo "$$version"; else echo "UNKNOWN"; fi)

# Common prefix for version tag file names
VERSION_FILE_PREFIX:=$(OBJDIR).version_

# Tag file for the current version. This ensures the manifest is always up to date, even if no files have changed,
# as the manfiest can depend on this file, which is unique per version.
VERSION_FILE:=$(VERSION_FILE_PREFIX)$(VERSION)

# Verborsity settings. One to print commands, anything else to keep te build silent.
ifeq ($(V),1)
  Q:=
else
  Q:=@
endif

#############
# Debug
#############

#$(info WILDCARD $(wildcard $(LIBRARY)*))
#$(info LIBS_JAR $(LIBS_JAR))
#$(info LIBS_DIR $(LIBS_DIR))
#$(info LIBS_TAG $(LIBS_TAG))
#$(info SOURCE $(SOURCE))

#############
# Rules
#############

# Compile all the source files using the generated list into build/jar/
$(COMPILE): $(SOURCE_FILE) | $(JARDIR)$(DIRTAG)
	@echo "Compiling source files"
	$Q$(JAVAC) $(JAVAC_OPTS) -sourcepath $(subst $(SPACE),:,$(strip $(SRC))) -d $(JARDIR) -extdirs $(LIBRARY) @$<
	$Qtouch $@

# Rule to generate a list of source files that can be used by javac
$(SOURCE_FILE): $(SOURCE) | $(JARDIR)$(DIRTAG)
	@echo "Generating list of source files $(@F)"
	@echo $(SOURCE) > $@

# Rule to extract a JAR to the build directory
# They are extracted to a temp directory, so the manifest can be removed without clashing with our own manifest
$(OBJDIR).tag_%: $(LIBRARY)%.jar | $(JARDIR)$(DIRTAG)
	@echo "Extracting $(*F)"
	$Qmkdir $(OBJDIR)$(*F)
	$Qcd $(OBJDIR)$(*F); $(JAR) xf ../../$<
	$Q$(RM) -r $(OBJDIR)$(*F)/META-INF/
	$Qcp -r $(OBJDIR)$(*F)/* $(JARDIR)
	$Q$(RM) -r $(OBJDIR)$(*F)
	$Qtouch $@

# Rule to build the jar manifest
$(MANIFEST): $(VERSION_FILE) $(SOURCE_MANIFEST_FILES)
	@cp -aur $(SOURCE_MANIFEST_DIR) $(MANIFEST_DIR)
	@echo "Creating manifest $(@F)"
	@echo "Manifest-Version: 1.0" > $@
	@echo "Class-Path: ." >> $@
	@echo "Sealed: true" >> $@
	@echo "Main-Class: $(MAIN)" >> $@
	@echo "Implementation-Version: $(VERSION)" >> $@

# Rule to create the version file. We only need to touch it to mark it up to date, and delete previous version files.
$(VERSION_FILE): | $(OBJDIR)$(DIRTAG)
	$Q$(RM) $(VERSION_FILE_PREFIX)*
	$Qtouch $@

# Rule to build a jar
$(OUTPUT_NAME): $(COMPILE) $(LIBS_TAG) $(MANIFEST)
	@echo "Building $(@F)"
	$Q$(JAR) $(JAR_OPTS) $(MANIFEST) $@ -C $(OBJDIR)jar/ . 
	$Qprintf "\nBuild $(@F) completed successfully\n"

build: $(OUTPUT_NAME)

# Tag to build a directory and it's tag
%/$(DIRTAG):
	$Q$(MKDIR) $(@D)
	$Qtouch $@

# Clean target
.PHONY:clean
clean:
	@echo "Deleting all compiled files and removing build directory $(OBJDIR)"
	$Q$(RM) -r $(OBJDIR)
	$Q$(RM) $(OUTPUT_NAME)
