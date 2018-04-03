#!/usr/bin/env bash
clear

echo "Start compiling..."
javac -cp ./:../artifacts/IterativeParallelismTest.jar ru/ifmo/rain/bandarchuk/concurrency/*.java
javac -cp ./:../artifacts/ParallelMapperTest.jar ru/ifmo/rain/bandarchuk/concurrent/mapper/*.java

echo "Compiled, preparing for test..."
rm info/kgeorgiy/java/advanced/concurrent/*.class >> log.txt
rm info/kgeorgiy/java/advanced/mapper/*.class >> log.txt


echo "Running tests..."
java -cp ./:../artifacts/ParallelMapperTest.jar:../lib/hamcrest-core-1.3.jar:../lib/jsoup-1.8.1.jar:../lib/junit-4.11.jar:../lib/quickcheck-0.6.jar info.kgeorgiy.java.advanced.mapper.Tester $1 ru.ifmo.rain.bandarchuk.concurrent.mapper.ParallelMapperImpl,ru.ifmo.rain.bandarchuk.concurrency.IterativeParallelism $2

rm ru/ifmo/rain/bandarchuk/concurrency/*.class >> log.txt
rm ru/ifmo/rain/bandarchuk/concurrent/mapper/*.class >> log.txt
rm log.txt