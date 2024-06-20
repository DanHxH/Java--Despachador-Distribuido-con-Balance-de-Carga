import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static List<Socket> clientes = new ArrayList<>();
    private static String msjRecibido = null;
    private static List<Integer> registroCargaActual = new ArrayList<Integer>();
    private static int clientesActuales = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket servidor = new ServerSocket(4040);
        System.out.println("Servidor iniciado");
        int aux = 1;

        while (true) {
            Socket sc = servidor.accept();
            clientes.add(sc);

            clientesActuales++;

            System.out.println("Cliente conectado");
            // System.out.println("Número de clientes: " + clientesActuales);
            registroCargaActual.add(0);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataInputStream in = new DataInputStream(sc.getInputStream());
                        while (true) {
                            // String mensaje = in.readUTF();
                            msjRecibido = in.readUTF();

                            String[] splitMsj = msjRecibido.split("-");
                            if (splitMsj[0].equals("p")) {// si el mensaje recibido es un proceso, then

                                int clienteDestino = getMenosCargado(registroCargaActual);
                                Socket socket = clientes.get(clienteDestino); // obtiene el primer cliente en
                                // la lista
                                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                out.writeUTF(msjRecibido);

                                String name = splitMsj[1];
                                int clienteOrigen = Integer.parseInt(splitMsj[3]);

                                if (clienteOrigen == clienteDestino)
                                    System.out.println("El proceso " + name
                                            + " fue enviado a su nodo cliente de origen (" + clienteOrigen + ")");
                                else if (clienteDestino == 0)
                                    System.out.println("El proceso " + name + " del cliente " + clienteOrigen
                                            + ", fue enviado al cliente 0 ");
                                else
                                    System.out.println("El proceso " + name + " del cliente " + clienteOrigen
                                            + ", fue enviado al cliente " + clienteDestino / 2);

                            }

                            if (splitMsj[0].equals("c")) { // si el mensaje recibido es un mensaje de carga actual
                                registroCargaActual.set((Integer.parseInt(splitMsj[2])) * 2,
                                        Integer.parseInt(splitMsj[1]));
                            }

                            if (splitMsj[0].equals("t")) {// si el mensaje recibido es una confirmación de terminación,
                                String name = splitMsj[1];
                                int clienteOrigen = Integer.parseInt(splitMsj[2]);
                                int clienteEjecucion = Integer.parseInt(splitMsj[3]);
                                if (clienteEjecucion == clienteOrigen)
                                    System.out.println(
                                            "Proceso " + name + ". Ejecutado en su cliente de origen ha finalizado.");
                                else
                                    System.out.println("Proceso " + name + " del cliente " + clienteOrigen
                                            + ". Ejecutado en el cliente " + clienteEjecucion + " ha finalizado.");

                                // enviamos la confirmación al cliente de origen
                                Socket socket = clientes.get(clienteOrigen * 2);
                                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                out.writeUTF(msjRecibido);

                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();
        }
    }

    private static int getMenosCargado(List<Integer> arr) {
        int min = arr.get(0);
        int index = 0;
        for (int i = 2; i < arr.size(); i = i + 2) {
            if (arr.get(i) < min) {
                min = arr.get(i);
                index = i;
            }
        }
        return index;
    }
}