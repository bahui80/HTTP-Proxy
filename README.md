PDC-Proxy
=========

Desde Eclipse
1.	El archivo de configuración del proxy se encuentra en src/main/resources y se llama proxy.properties.
2.	Los logs se encuentran en la carpeta log.
3.	Para ejecutar el proxy server basta con ejecutar la clase TCPServerSelector.

Generando el jar
1.	Situarse donde se encuentra el archivo pom.xml.
2.	Allí desde una consola ejecutar el siguiente comando: mvn clean package. Esto generará el jar correspondiente.
3.	Finalizado el paso anterior ingresar a la carpeta generada (llamada target).
4.	El archivo de configuración del proxy se encuentra en clases y se llama proxy.properties.
5.	Los logs se encuentran en la carpeta log.
6.	Para ejecutar el proxy server basta con situarse en la carpeta target y ejecutar el siguiente comando: java –jar
	HTTP-Proxy-1.0jar-with-dependencies.jar.

Desde un User Agent con configuración previa
1.	Abrir el archivo proxy.properties y configurar la IP local.
2.	Abrir el user agent deseado e ir a configuración de proxy.
3.	Una vez allí activar la opción de usar un servidor proxy.
4.	Introducir la IP local y el puerto 9090.
5.	Correr el proxy server.
6.	Comenzar a navegar por la web (sin usar páginas https).

Desde netcat
1.	Ejecutar el proxy server.
2.	Ejecutar una terminal e introducir nc [página solicitada] 9090
3.	Introducir GET / HTTP/1.1
4.	Introducir Host: [página solicitada]
5.	Introducir algún otro header que se quiera enviar.

Encadenamiento de proxies
1.	Abrir el archivo proxy.properties y configurar el server-ip con la dirección IP local (es decir donde se está coriendo el proxy).
2.	Configurar el chained-ip con la dirección IP del proxy al cual se desea encadenarse.
3.	Configurar el chained-port con el puerto en el cual está corriendo el proxy al que se desea encadenar.
4.	Continuar con alguno de los casos anteriores.
