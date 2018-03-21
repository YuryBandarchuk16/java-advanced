#!/usr/bin/env bash
clear

javac -cp ./:../artifacts/JarImplementorTest.jar:../lib/hamcrest-core-1.3.jar:../lib/jsoup-1.8.1.jar:../lib/junit-4.11.jar:../lib/quickcheck-0.6.jar ./info/kgeorgiy/java/advanced/implementor/*.java ./ru/ifmo/rain/bandarchuk/implementor/*.java ./info/kgeorgiy/java/advanced/implementor/examples/basic/*.java ./info/kgeorgiy/java/advanced/implementor/standard/basic/*.java

echo "Manifest-Version: 1.0" > MANIFEST.TXT
echo "Main-Class: ru.ifmo.rain.bandarchuk.implementor.Implementor" >> MANIFEST.TXT

jar cfm Implementor.jar MANIFEST.TXT ./info/kgeorgiy/java/advanced/implementor/ImplerException.class  ./info/kgeorgiy/java/advanced/implementor/Impler.class ./info/kgeorgiy/java/advanced/implementor/JarImpler.class ./ru/ifmo/rain/bandarchuk/implementor/*.class

rm info/kgeorgiy/java/advanced/implementor/*.class
rm info/kgeorgiy/java/advanced/implementor/examples/basic/*.class
rm info/kgeorgiy/java/advanced/implementor/examples/full/*.class
rm info/kgeorgiy/java/advanced/implementor/standard/basic/*.class
rm info/kgeorgiy/java/advanced/implementor/standard/full/*.class
rm info/kgeorgiy/java/advanced/base/*.class
rm ru/ifmo/rain/bandarchuk/implementor/*.class

echo "Jar has been successfully created!"


