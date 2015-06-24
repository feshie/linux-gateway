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

# Manifest file for JAR
MANIFEST:=$(OBJDIR)jar/META-INF/MANIFEST.MF

# File used as a tag for directory creation
DIRTAG:=.dirtag

# File that holds a list of source files
SOURCE_FILE:=$(OBJDIR).files

# Tag for compilation dates of java source
COMPILE:=$(OBJDIR).compile

# Folder JAR will be made from
JARDIR:=$(OBJDIR)jar/

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
# Set default goal
.DEFAULT_GOAL:=build

# Do not echo commands
.SILENT:

# Delete target on error
.DELETE_ON_ERROR:

# Compile all the source files using the generated list into build/jar/
$(COMPILE): $(SOURCE_FILE) | $(JARDIR)$(DIRTAG)
	echo "Compiling source files"
	$(JAVAC) $(JAVAC_OPTS) -sourcepath $(subst $(SPACE),:,$(strip $(SRC))) -d $(JARDIR) -extdirs $(LIBRARY) @$<
	touch $@

# Rule to generate a list of source files that can be used by javac
$(SOURCE_FILE): $(SOURCE) $(ANTLR_COMPILE) | $(JARDIR)$(DIRTAG)
	echo "Generating list of source files $(@F)"
	echo $(SOURCE) > $@

# Rule to copy include dirs to build directory
$(OBJDIR).tag_%: $(LIBRARY)%
	cp -r $^ $(JARDIR)
	touch $@

# Rule to extract a JAR to the build directory
$(OBJDIR).tag_%: $(LIBRARY)%.jar
	echo "Extracting $(<F)"
	cd $(JARDIR); \
	$(JAR) xf ../../$<; \
	$(RM) -r META-INF/
	touch $@

# Rule to build the jar manifest
$(MANIFEST): $(LIBS_TAG) | $(OBJDIR)$(DIRTAG)
	$(MKDIR) $(@D)
	echo "Creating manifest $(@F)"
	echo "Manifest-Version: 1.0" > $@
	echo "Class-Path: ." >> $@
	echo "Sealed: true" >> $@
	echo "Main-Class: $(MAIN)" >> $@

# Rule to build a jar
$(OUTPUT_NAME): $(COMPILE) $(LIBS_TAG) $(MANIFEST)
	echo "Building $(@F)"
	$(JAR) $(JAR_OPTS) $(MANIFEST) $@ -C $(OBJDIR)jar/ . 
	printf "\nBuild $(@F) completed successfully\n"

build: $(OUTPUT_NAME)

# Tag to build a directory and it's tag
%/$(DIRTAG):
	echo "Creating directory $(@D)"
	$(MKDIR) $(@D)
	touch $@

# Clean target
.PHONY:clean
clean:
	echo "Deleting all compiled files and removing build directory $(OBJDIR)"
	$(RM) -r $(OBJDIR)
	$(RM) $(OUTPUT_NAME)