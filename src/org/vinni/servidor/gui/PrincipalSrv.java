package org.vinni.servidor.gui;

import org.vinni.dto.MiDatagrama;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrincipalSrv extends JFrame {

    private final int PORT = 12345;

    // Guardar clientes conectados
    private Set<String> clientes = new HashSet<>();

    public PrincipalSrv() {
        initComponents();
        this.mensajesTxt.setEditable(false);
    }

    private void initComponents() {

        this.setTitle("Servidor ...");

        bIniciar = new JButton();
        jLabel1 = new JLabel();
        mensajesTxt = new JTextArea();
        jScrollPane1 = new JScrollPane();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        bIniciar.setFont(new java.awt.Font("Segoe UI", 0, 18));
        bIniciar.setText("INICIAR SERVIDOR");
        bIniciar.addActionListener(evt -> bIniciarActionPerformed(evt));
        getContentPane().add(bIniciar);
        bIniciar.setBounds(150, 50, 250, 40);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));
        jLabel1.setText("SERVIDOR UDP : FERINK");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(150, 10, 200, 17);

        mensajesTxt.setColumns(25);
        mensajesTxt.setRows(5);

        jScrollPane1.setViewportView(mensajesTxt);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(20, 150, 500, 120);

        setSize(new java.awt.Dimension(570, 320));
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new PrincipalSrv().setVisible(true);
        });
    }

    private void bIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        iniciar();
    }

    public void iniciar(){

        mensajesTxt.append("Servidor UDP iniciado en puerto "+PORT+"\n");

        new Thread(() -> {

            try {

                DatagramSocket socketudp = new DatagramSocket(PORT);
                this.bIniciar.setEnabled(false);

                while (true) {

                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    socketudp.receive(dp);

                    String mensaje = MiDatagrama.obtenerMensaje(dp);

                    String ip = dp.getAddress().getHostAddress();
                    int puerto = dp.getPort();

                    String clienteKey = ip + ":" + puerto;

                    // registrar cliente
                    clientes.add(clienteKey);

                    mensajesTxt.append("Mensaje recibido de " + clienteKey + ": " + mensaje + "\n");

                    // MENSAJE PRIVADO
                    if(mensaje.startsWith("@")){

                        try{

                            String[] partes = mensaje.split(" ",2);

                            int puertoDestino = Integer.parseInt(partes[0].substring(1));
                            String contenido = "[Privado de "+puerto+"]: "+partes[1];

                            DatagramPacket privado = MiDatagrama.crearDataG(ip, puertoDestino, contenido);
                            socketudp.send(privado);

                            mensajesTxt.append("Mensaje privado enviado a "+puertoDestino+"\n");

                        }catch(Exception e){
                            mensajesTxt.append("Error en formato de mensaje privado\n");
                        }

                    }else{

                        // MENSAJE GLOBAL
                        for(String c : clientes){

                            String[] partes = c.split(":");

                            String ipDestino = partes[0];
                            int puertoDestino = Integer.parseInt(partes[1]);

                            DatagramPacket mensajeGlobal = MiDatagrama.crearDataG(
                                    ipDestino,
                                    puertoDestino,
                                    "["+puerto+" dice]: "+mensaje
                            );

                            socketudp.send(mensajeGlobal);
                        }

                    }

                }

            } catch (SocketException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            }

        }).start();

    }

    private JButton bIniciar;
    private JLabel jLabel1;
    private JTextArea mensajesTxt;
    private JScrollPane jScrollPane1;

}