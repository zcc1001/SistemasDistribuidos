#!/usr/bin/env bash

java -cp ./target/Practica2ChatRMI-1.0-SNAPSHOT.jar \
  -Dweblogic.oif.serialFilterMode=combine \
  -Dweblogic.oif.serialFilterScope=weblogic \
  -Dweblogic.oif.serialFilter=maxDepth=10000 \
  -Djava.security.policy=registerit.policy \
  -Djava.rmi.server.codebase=http://localhost:8080/Practica2ChatRMI-Web/ \
  es.ubu.lsi.server.ChatServerDynamic
