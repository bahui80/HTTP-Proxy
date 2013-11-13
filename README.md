PDC-Proxy
=========

Desde un User Agent con configuración previa
1.	Abrir el archivo proxy.properties (src/main/resources )  y configurar la IP local.
2.	Abrir el user agent deseado e ir a configuración de proxy.
3.	Una vez allí activar la opción de usar un servidor proxy.
4.	Introducir la IP local y el puerto 9090.
5.	Ejecutar la clase TCPServerSelector.
6.	Comenzar a navegar por la web (sin usar páginas https).
Desde netcat
1.	Ejecutar la clase TCPServerSelector.
2.	Ejecutar una terminal e introducir nc [página solicitada] 9090
3.	Introducir GET / HTTP/1.1
4.	Introducir Host: [página solicitada]
5.	Introducir algún otro header que se quiera enviar.
Encadenamiento de proxies
1.	Abrir el archivo proxy.properties y configurar el server-ip con la dirección IP local (es decir donde se está coriendo el proxy). 
2.	Configurar el chained-ip con la dirección IP del proxy al cual se desea encadenarse.
3.	Configurar el chained-port con el puerto en el cual está corriendo el proxy al que se desea encadenar.
4.	Continuar con alguno de los casos anteriores 
