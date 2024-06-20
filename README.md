# Despachador Distribuido con Balance de Carga

Este proyecto implementa un sistema distribuido para la ejecución de procesos en varios nodos, utilizando un despachador que asegura el balance de carga entre los nodos disponibles. El sistema consiste en un cliente y un servidor que se comunican a través de sockets TCP/IP.

## Características

- **Cliente:** Permite enviar procesos con nombre y tiempo de ejecución al servidor. Utiliza un algoritmo Round Robin para distribuir los procesos entrantes entre los nodos disponibles.
  
- **Servidor:** Gestiona múltiples conexiones de clientes y distribuye los procesos entrantes al nodo menos cargado. Cada nodo reporta su carga actual al servidor para tomar decisiones de despacho.
  
- **Balance de Carga:** Implementa un algoritmo simple para determinar qué nodo recibirá cada proceso, asegurando que los nodos menos ocupados sean seleccionados primero.

## Tecnologías Utilizadas

- Java para la implementación del cliente y servidor.
- Sockets TCP/IP para la comunicación entre componentes.
- Multihilo para manejar múltiples conexiones simultáneas.

## Uso

1. **Clonar el repositorio:**
   git clone https://github.com/tu-usuario/despachador-distribuido.git

2. **Compilar y ejecutar el servidor:**
   javac Servidor.java
   java Servidor

3. **Compilar y ejecutar el cliente:**
   javac Cliente.java
   java Cliente [número de cliente]

Sustituye `[número de cliente]` con el identificador del cliente que deseas usar.

## Contribución

Las contribuciones son bienvenidas. Si tienes alguna idea para mejorar este proyecto, por favor abre un issue para discutir tu propuesta.