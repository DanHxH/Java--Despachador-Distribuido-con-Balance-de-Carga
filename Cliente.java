import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.net.*;

public class Cliente {
    private static String numCliente;
    private static Queue<String> procesosEntrantes;
    private static int cargaActual = 0;

    public static void main(String[] args) throws IOException {

        if (args.length == 0) { // no hay argumentos en el programa
            System.err.println("\n<<<<<<<Usage: cliente [número de cliente]>>>>>>>\n");
            System.exit(1);
        }

        // se inicializa la cola de procesos
        procesosEntrantes = new LinkedList<String>();

        // Se inician los hilos
        recibidorMensajes recMsj = new recibidorMensajes(startClientSocket(4040)); // Se conecta al servidor y crea un
                                                                                   // hilo para recibir mensajes
        recMsj.start(); // Inicia el hilo recibidor

        numCliente = args[0];
        System.out.print("\n---------------No. Cliente: ");
        System.out.print(numCliente);
        System.out.println(" ---------------");

        Socket sc = startClientSocket(4040); // Se conecta al servidor
        DataOutputStream out = startOutputStream(sc);

        RoundRobin roundR = new RoundRobin(procesosEntrantes, sc);
        roundR.start(); // Inicia el hilo de round robin

        // recibidorMensajes recMsj = new recibidorMensajes(sc);

        String dato = null;
        while (true) {
            for (int i = 0; i <= 3; i++) { // idTipodeMensaje-nombreProceso-tiempo-clienteOigen
                if (i == 0)
                    dato = "p-"; // id que identifica a un proceso
                if (i == 1)
                    dato = dato + pedirNombreProc() + "-";
                if (i == 2)
                    dato = dato + pedirTiempoProc() + "-";
                if (i == 3)
                    dato = dato + numCliente;
            }
            enviarDato(dato, out);

        }
    }

    private static String pedirNombreProc() {
        Scanner Scanner = new Scanner(System.in);
        System.out.print("\nNombre del proceso: ");
        String nombre = Scanner.nextLine();
        return nombre;
    }

    private static String pedirTiempoProc() {
        Scanner Scanner = new Scanner(System.in);
        System.out.print("Tiempo del proceso: ");
        String tiempo = Scanner.nextLine();
        return tiempo;

    }

    private static Socket startClientSocket(int port) throws IOException {
        Socket sc = new Socket("localhost", port);
        return sc;
    }

    private static DataOutputStream startOutputStream(Socket sc) throws IOException {
        DataOutputStream out = new DataOutputStream(sc.getOutputStream());
        return out;
    }

    private static DataInputStream startInputStream(Socket sc) throws IOException {
        DataInputStream in = new DataInputStream(sc.getInputStream());
        return in;
    }

    public static void enviarDato(String dato, DataOutputStream out) throws IOException {
        out.writeUTF(dato);
    }
    
    private static class RoundRobin extends Thread {
        private Queue<String> colaEntrantes;
        private Socket socket;

        public RoundRobin(Queue<String> colaEntrantes, Socket socket) {
            this.colaEntrantes = colaEntrantes;
            this.socket = socket;
        }

        public void run() {
            try {
                // colaEntrantes.add("p-Proceso2-5-02");
                // colaEntrantes.add("p-Proceso3-8-03");
                int quantum = 5;

                while (true) {

                    cargaActual = 0; // se reinicia la carga actual para que pueda ser calculada nuevamente
                    for (String elemento : colaEntrantes) {
                        String[] aux = elemento.split("-");
                        cargaActual = cargaActual + Integer.parseInt(aux[2]);
                        // System.out.println(elemento);
                    }
                    // Enviar carga actual
                    String msjCargaActual = "c-" + cargaActual + "-" + numCliente;// mensaje de la carga actual

                    try {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF(msjCargaActual);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    System.out.println("Carga actual: " + cargaActual);

                    String mensaje = colaEntrantes.poll();
                    if (mensaje != null) {
                        String[] process = mensaje.split("-"); // Dividimos la string
                        String name = process[1]; // posición 1 del string está el nombre
                        int time = Integer.parseInt(process[2]); // posición 2 del string está el tiempo
                        String clienteOrigen = process[3]; // posicion 3 es el cliente origen
                        // System.out.println("Cliente Origen: " + clienteOrigen);
                        if (time > quantum) {
                            System.out.println(
                                    "\nEjecutando proceso: " + name + " -->Tiempo Restante: " + time + " segundos.");
                            Thread.sleep(quantum * 1000);
                            time -= quantum;

                            colaEntrantes.add("p-" + name + "-" + time + "-" + clienteOrigen);// se vuelve a añadir a la
                                                                                              // cola
                            // para la
                            // sig vuelta
                        } else {
                            System.out.println(
                                    "\nEjecutando proceso: " + name + " -->Tiempo Restante: " + time + " segundos.");
                            Thread.sleep(time * 1000);
                            time -= quantum;
                            if (time <= 0) {
                                System.out.println("Ejecución local: " + name + " terminado.");
                                // Enviar confirmación de terminación
                                String clienteEjecución = numCliente;
                                try {
                                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                    out.writeUTF("t-" + name + "-" + clienteOrigen + "-" + clienteEjecución);
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }

                            }
                        }
                    } else {
                        System.out.println("\nCola Vacía. Esperando.");
                        Thread.sleep(5 * 1000);
                    }

                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

    }

    private static class recibidorMensajes extends Thread {
        private Socket socket;

        public recibidorMensajes(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                while (true) {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String mensaje = in.readUTF();
                    // System.out.println("\n");
                    // System.out.println(mensaje);

                    // si el mensaje recibido es un proceso meterlo en la cola de recibidos
                    String[] splitMsj = mensaje.split("-");

                    if (splitMsj[0].equals("p"))
                        procesosEntrantes.offer(mensaje);

                    if (splitMsj[0].equals("t")) {
                        String name = splitMsj[1];
                        System.out.println("Ejecución remota: " + name + " terminado.");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
