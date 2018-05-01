#!/usr/bin/env bash
clear

echo "Start compiling..."
javac -cp ./:../artifacts/WebCrawlerTest.jar:../lib/jsoup-1.8.1.jar ru/ifmo/rain/bandarchuk/crawler/*.java

echo "Compiled, preparing for test..."
rm info/kgeorgiy/java/advanced/crawler/*.class >> log.txt


echo "Running tests..."
java -cp ./:../artifacts/WebCrawlerTest.jar:../lib/hamcrest-core-1.3.jar:../lib/jsoup-1.8.1.jar:../lib/junit-4.11.jar:../lib/quickcheck-0.6.jar info.kgeorgiy.java.advanced.crawler.Tester $1 ru.ifmo.rain.bandarchuk.crawler.WebCrawler $2

rm ru/ifmo/rain/bandarchuk/crawler/*.class >> log.txt
rm log.txt
