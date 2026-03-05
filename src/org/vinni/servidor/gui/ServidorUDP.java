package org.vinni.servidor.gui;

import org.vinni.dto.MiDatagrama;
import java.net.*;
import java.util.*;

public class ServidorUDP {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(12345);
        // Guardamos los clientes como "IP:Puerto"
        Set<String> clientes = new HashSet<>();
        byte[] buffer = new byte[1024];

        System.out.println("Servidor UDP iniciado en puerto 12345...");

        while (true) {
            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
            socket.receive(peticion);

            String clientKey = peticion.getAddress().getHostAddress() + ":" + peticion.getPort();
            clientes.add(clientKey);

            String mensaje = MiDatagrama.obtenerMensaje(peticion);
            System.out.println("Recibido de " + clientKey + ": " + mensaje);

            // Lógica de Direccionamiento
            if (mensaje.startsWith("@")) {
                // Mensaje Privado: Formato "@puerto mensaje"
                try {
                    String[] partes = mensaje.split(" ", 2);
                    int puertoDestino = Integer.parseInt(partes[0].substring(1));
                    String contenido = "[Privado de " + peticion.getPort() + "]: " + partes[1];

                    DatagramPacket pvp = MiDatagrama.crearDataG("127.0.0.1", puertoDestino, contenido);
                    socket.send(pvp);
                } catch (Exception e) {
                    System.out.println("Error en formato privado.");
                }
            } else {
                // Mensaje Global (Broadcast a todos los conocidos)
                for (String c : clientes) {
                    String[] parts = c.split(":");
                    DatagramPacket p = MiDatagrama.crearDataG(parts[0], Integer.parseInt(parts[1]),
                            "[" + peticion.getPort() + " dice]: " + mensaje);
                    socket.send(p);
                }
            }
        }
    }
}
