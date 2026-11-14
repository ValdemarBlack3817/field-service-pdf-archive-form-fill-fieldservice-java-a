#!/bin/sh
set -eu
rm -rf out-test
mkdir -p out-test
javac -d out-test $(find src -name '*.java')
java -cp out-test fieldservice.WorkOrderPdfServiceTest
