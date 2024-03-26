# Practica 1 - EPO1-1C - Chat Sockets

Practica 1 de la asignatura Sistemas distribuidos UBU.

# Requerimientos

- Java 17
- Maven
- Ant

# Instrucciones de arranque

## Usando maven plugins

Se puede arrancar el servidor/cliente utilizando `exec-maven-plugin` para esto esta previamente configurado tres
comandos distinto uno para el servidor y otros 2 para crear 2 clientes

```shell
# compilar generar jar
mvn clean install

# inicializa el servidor
mvn exec:java@server

# inicializa un cliente con username = blas
mvn exec:java@cliente-blas

# inicializa un cliente con username = pio
mvn exec:java@cliente-pio
```

si quisieramos instanciar el servidor con parametros customizados podemos utilizar la siguiente instruccion:

```shell
 mvn compile exec:java -Dexec.mainClass="es.ubu.lsi.server.ChatServerImpl" -Dexec.args="2000"
```

donde en `-Dexec.args` podemos indicar los argumentos a pasar al programa, en este caso el primer argumento indica el *
*puerto** a utilizar

si quisieramos instanciar algun otro cliente con una configuracion customizada podemos utilizar la siguiente
instruccion:

```shell
 mvn compile exec:java -Dexec.mainClass="es.ubu.lsi.client.ChatClientImpl" -Dexec.args="localhost 2000 pedro"
```

donde en `-Dexec.args` podemos indicar los argumentos a pasar al programa, en este caso sigue la siguiente regla:

- si solo se envia un arg, entonces este se utilizara para indicar **username**
- si se envian 2 args, el primero indica el **puerto** y el segundo el **username**
- si se envian 3 args, el primero indica la **direccion del servidor** el segundo el **puerto** y el tercero el *
  *username**

## Generar javadoc

Para generar la documentacion utilizamos `ant`

```shell
ant generar
```


