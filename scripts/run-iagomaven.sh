#/bin/sh

# I don't know maven... blindly automating the "how to run" Iago example tests instructions...

# make sure to fail on any errors
set -e

# basic arg checking

if [ $# -ne 4  ] ; then
 echo "Wrong number of args!"
		 echo "Got count $# with content:"
 for word in "$@"; do echo "$word"; done

 echo "runmavenpackage.sh project_base module_name debug|nodebug 'jar arguments'"
# echo "for IntelliJ this would be the macros \$ProjectFileDir\$ $ModuleName$ '\$ModuleName\$-1.0.jar -f config/your_config.scala'"
# echo "NOTE: I couldn't figure out how to pass parameters to maven lifecycle steps or the above macros in to external commands.... TBD"
exit 1
fi

BASE=$1
MODULE=$2
DEBUG=$3
JAR=$4

echo "Starting run with parameters:"
echo "project_base= $BASE"
echo "module_name= $MODULE"
echo "debug= $DEBUG"
echo "jar and arguments= $JAR"
echo

# setup java remote debugging - assuming java debugger is on localhost
# copied from the IntelliJ hints for setting up remote java debugging
# Note I could never get attach to work- so you have to start the test and then quickly connect from IntelliJ
if [ "$DEBUG" = "debug" ] ; then
	DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
else
	DEBUG_OPTS=""
fi

export DEBUG_OPTS

echo "Moving to $BASE..."
cd $BASE

echo "Cleaning up any previous runs..."
rm -rf "target/tmp"

echo "Building..."

mvn package -DskipTests

echo "Unpacking..."
mkdir "target/tmp"
cd "target/tmp"
unzip "../${MODULE}-package-dist.zip"

echo "Executing with DEBUG_OPTS=$DEBUG_OPTS"
java -jar $JAR

echo "Shutting everythign down..."
java -jar $JAR -k