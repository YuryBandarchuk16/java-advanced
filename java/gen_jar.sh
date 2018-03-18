rm -rf generatedJar

mkdir generatedJar
cd generatedJar

javac ../info/kgeorgiy/java/advanced/implementor/*.java ../ru/ifmo/rain/bandarchuk/implementor/*.java ../info/kgeorgiy/java/advanced/implementor/examples/basic/*.java ../info/kgeorgiy/java/advanced/implementor/standard/basic/*.java 

echo "Manifest-Version: 1.0" > MANIFEST
echo "Main-Class: ru.ifmo.rain.bandarchuk.implementor.Implementor" >> MANIFEST

jar cfm Implementor.jar \
		MANIFEST
		../info/kgeorgiy/java/advanced/implementor/examples/basic/*.class \
		../info/kgeorgiy/java/advanced/implementor/standard/basic/*.class \
		../info/kgeorgiy/java/advanced/implementor/ImplerException.class \
		../info/kgeorgiy/java/advanced/implementor/Impler.class \
		../info/kgeorgiy/java/advanced/implementor/JarImpler.class \
		../ru/ifmo/rain/bandarchuk/implementor/*.class

cd ..
rm info/kgeorgiy/java/advanced/implementor/*.class
rm ru/ifmo/rain/bandarchuk/implementor/*.class


