#!/bin/bash
PROJECT_ROOT=`dirname $0`/..
cd "$PROJECT_ROOT"
#build it
mvn clean
mvn package
mvn source:jar
mvn javadoc:jar
#sign produced files and pom.xml
for jar in `ls "target/*.jar"`
do
	gpg --clearsign "$jar"
done
gpg --clearsign pom.xml
#create the bundle
rm -rf tmp 2> /dev/null
mkdir tmp
cp pom.xml tmp
mv pom.xml.asc tmp
cp target/*.jar tmp
cp target/*.asc tmp
cp pom.xml tmp
cp pom.xml.asc tmp
ls tmp
zip -9 -y -r -q bundle.zip tmp/
rm tmp -rf