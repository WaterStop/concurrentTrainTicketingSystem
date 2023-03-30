#!/bin/sh

for i in $(seq 1 100)
do
	echo 'execution NO.'$i
	sh ./clean.sh
	javac -encoding UTF-8 -cp . ticketingsystem/GenerateHistory.java
	java -cp . ticketingsystem/GenerateHistory 64 10000 1 0 0 > trace
	java -jar VeriLinS.jar 64 trace 1 history
done
