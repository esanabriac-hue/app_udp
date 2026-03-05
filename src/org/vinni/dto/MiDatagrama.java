package org.vinni.dto;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class MiDatagrama {
    public static DatagramPacket crearDataG(String ip, int puerto, String mensaje) {
        try {
            InetAddress direccion = InetAddress.getByName(ip);
            byte[] mensajeB = mensaje.getBytes();
            return new DatagramPacket(mensajeB, mensajeB.length, direccion, puerto);
        } catch (Exception ex) {
            return null;
        }
    }
    public static String obtenerMensaje(DatagramPacket p) {
        return new String(p.getData(), 0, p.getLength());
    }
}