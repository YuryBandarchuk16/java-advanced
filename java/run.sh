clear

echo "Start compiling..."
javac ru/ifmo/rain/bandarchuk/implementor/Implementor.java 

echo "Compiled, preparing for test..."
rm info/kgeorgiy/java/advanced/implementor/*.class

echo "Running tests..."
java -cp ./:../artifacts/ImplementorTest.jar:../lib/hamcrest-core-1.3.jar:../lib/jsoup-1.8.1.jar:../lib/junit-4.11.jar:../lib/quickcheck-0.6.jar info.kgeorgiy.java.advanced.implementor.Tester $1 ru.ifmo.rain.bandarchuk.implementor.Implementor $2


