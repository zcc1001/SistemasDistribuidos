#!/usr/bin/env bash

java -cp ./target/Practica2ChatRMI-1.0-SNAPSHOT.jar \
  -Djava.security.manager=allow \
  -Djava.security.policy=registerit.policy \
  -Djava.rmi.server.codebase=http://localhost:8080/Practica2ChatRMI-Web/ \
  es.ubu.lsi.client.ChatClientDynamic $1
