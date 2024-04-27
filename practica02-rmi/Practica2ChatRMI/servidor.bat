set CLASSPATH=.\target\Practica2ChatRMI-1.0-SNAPSHOT.jar

java -Djava.security.policy=registerit.policy -Djava.security.manager=allow ^
-Djava.rmi.server.codebase=http://localhost:8080/Practica2ChatRMI-Web/ ^
es.ubu.lsi.server.ChatServerDynamic