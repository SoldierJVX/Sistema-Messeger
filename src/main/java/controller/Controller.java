/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import model.CaixaSom;
import model.ImageSerializable;
import model.Microfone;
import view.AudioAtivado;
import view.CompartilhamentoTela;
import view.InterfaceGeral;
import view.PainelArquivos;

/**
 *
 * @author SoldierJVX
 */
public abstract class Controller {

    HashMap<String, InterfaceGeral> view = new HashMap<String, InterfaceGeral>();
    HashMap<String, ServerSocket> servers = new HashMap<>();
    HashMap<String, Socket> sockets = new HashMap<>();
    HashMap<String, DataInput> inputs = new HashMap<>();
    HashMap<String, DataOutput> outputs = new HashMap<>();
    String ipDestino;
    boolean mensageiro = false;

    ObjectOutputStream saida;
    ObjectInputStream entrada;

    HashMap<String, JFileChooser> arquivos = new HashMap<>();

    protected void notificarEnvioArquivo() {
        try {

            if (arquivos.get("envioArquivo") == null) {

                arquivos.put("envioArquivo", new JFileChooser());

                arquivos.get("envioArquivo").setCurrentDirectory(new File("/"));
                arquivos.get("envioArquivo").setFileSelectionMode(JFileChooser.FILES_ONLY);

            }

            arquivos.get("envioArquivo").showOpenDialog(null);

            outputs.get("out").writeUTF("RECEBER ARQUIVO");

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void enviarArquivo(String acesso) {

        try {
            String partes[] = acesso.split("-");

            String[] novaLinha = {
                new String("Output"),
                new String(arquivos.get("envioArquivo").getSelectedFile().getName()),
                new String(arquivos.get("envioArquivo").getSelectedFile().length() + ""),
                new String("0")

            };

            DefaultTableModel model = (DefaultTableModel) ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().getModel();
            int pos = model.getRowCount();
            model.addRow(novaLinha);
            ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().setModel(model);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //Enviando nome arquivo
                        outputs.get("outArq").writeUTF(arquivos.get("envioArquivo").getSelectedFile().getName());

                        //Enviando tamanho arquivo
                        outputs.get("outArq").writeUTF(arquivos.get("envioArquivo").getSelectedFile().length() + "");

                        //Enviando arquivo
                        FileInputStream arquivo = new FileInputStream(arquivos.get("envioArquivo").getSelectedFile().getAbsolutePath());

                        int b;
                        int totalEnviados = 0;
                        while ((b = arquivo.read()) != -1) {
                            outputs.get("outArq").write(b);
                            totalEnviados++;

                            if (totalEnviados % 1000 == 0) {
                                atualizarLinhaTabela(totalEnviados);
                            }

                        }

                        atualizarLinhaTabela(totalEnviados);

                        arquivo.close();

                        ((DataOutputStream) outputs.get("outArq")).flush();

                    } catch (SocketException ex2) {
                        System.exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                private void atualizarLinhaTabela(int totalEnviados) {
                    DefaultTableModel model = (DefaultTableModel) ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().getModel();
                    model.setValueAt(totalEnviados + "", pos, 3);
                    ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().setModel(model);
                }
            }).start();
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void actionReceberArquivo() {

        try {

            String mensagem = "RECEIVEARQ " + ipDestino + "-" + servers.get("recebeArqServer").getLocalPort();
            outputs.get("out").writeUTF(mensagem);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        String nomeArquivo = inputs.get("inArq").readUTF();
                        String tamanhoArquivo = inputs.get("inArq").readUTF();

                        String[] novaLinha = {
                            new String("Input"),
                            new String(nomeArquivo),
                            new String(tamanhoArquivo),
                            new String("0")

                        };

                        DefaultTableModel model = (DefaultTableModel) ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().getModel();
                        int pos = model.getRowCount();
                        model.addRow(novaLinha);
                        ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().setModel(model);

                        FileOutputStream arquivo = new FileOutputStream(arquivos.get("salvar").getSelectedFile().getAbsolutePath() + "\\" + nomeArquivo);

                        int b;
                        int totalRecebidos = 0;
                        while (totalRecebidos != Integer.parseInt(tamanhoArquivo)) {
                            b = ((DataInputStream) inputs.get("inArq")).read();
                            arquivo.write(b);
                            totalRecebidos++;

                            if (totalRecebidos % 1000 == 0) {
                                atualizarLinhaTabela(totalRecebidos, pos);
                            }
                        }

                        atualizarLinhaTabela(totalRecebidos, pos);

                        arquivo.flush();
                        arquivo.close();

                    } catch (SocketException ex2) {
                        System.exit(0);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                private void atualizarLinhaTabela(int totalRecebidos, int pos) {
                    DefaultTableModel model = (DefaultTableModel) ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().getModel();
                    model.setValueAt(totalRecebidos + "", pos, 3);
                    ((PainelArquivos) view.get("painelArquivos")).getTblArquivos().setModel(model);
                }
            }).start();

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void definirDiretorioArquivos() {

        if (arquivos.get("salvar") == null) {

            arquivos.put("salvar", new JFileChooser());

            arquivos.get("salvar").setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        }

        arquivos.get("salvar").setCurrentDirectory(new File("/"));

        arquivos.get("salvar").showSaveDialog(null);

        ((PainelArquivos) view.get("painelArquivos")).getTxtDiretorioSalvar().setText(arquivos.get("salvar").getSelectedFile().getAbsolutePath());

    }

    protected void ativarAudio() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // definindo a porta de escuta de comunicacao
                    int porta = 8888;

                    // definindo um SOCKET UDP
                    DatagramSocket socket = new DatagramSocket(porta);
                    System.out.println("Servidor: Socket OK!");

                    CaixaSom caixaSom = new CaixaSom();

                    while (true) {
                        // criando pacote e recebendo da rede
                        byte[] conteudo = new byte[256];
                        DatagramPacket pacote = new DatagramPacket(conteudo, conteudo.length);

                        socket.receive(pacote);

                        // tirando a informacao da rede e colocando no seu programa
                        byte[] b = pacote.getData();

                        // tocar o som
                        caixaSom.tocar(b);
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // definindo PORTA e IP para enviar os dados
                    int porta = 8888;
                    InetAddress ip = InetAddress.getByName(ipDestino);

                    // Criando socket da comunicacao
                    DatagramSocket socket = new DatagramSocket();

                    Microfone microfone = new Microfone();

                    while (true) {
                        byte[] audio = microfone.ouvir();

                        DatagramPacket pacote = new DatagramPacket(audio, audio.length, ip, porta);
                        socket.send(pacote);
                    }
                } catch (SocketException ex2) {
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();

        if (view.get("audioAtivado") == null) {
            view.put("audioAtivado", new AudioAtivado());
        }

        view.get("audioAtivado").setVisible(true);

    }

    protected void compartilharTela() {

        if (view.get("telaCompartilhada") == null) {
            view.put("telaCompartilhada", new CompartilhamentoTela());
        }

        view.get("telaCompartilhada").setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        //Envia Tela
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    Robot robot = new Robot();

                    while (true) {

                        Thread.sleep((long) 33.3333);

                        BufferedImage image = (BufferedImage) robot.createScreenCapture(new Rectangle(width, height));

                        ImageSerializable imgSerializable = new ImageSerializable(image);

                        ((ObjectOutputStream) outputs.get("outComp")).writeObject(imgSerializable);
                        

                    }
                } catch (SocketException ex2) {
                    System.exit(0);
                } catch (AWTException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();

        // Recebe Tela
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {

                        ImageSerializable imgSerializable = (ImageSerializable) ((ObjectInputStream) inputs.get("inComp")).readObject();

                        BufferedImage image = imgSerializable.image;

                        ((CompartilhamentoTela) view.get("telaCompartilhada")).getLblCompartilhamento().setText("");
                        ((CompartilhamentoTela) view.get("telaCompartilhada")).getLblCompartilhamento().setIcon(new ImageIcon(image));
                        

                    }
                } catch (Exception ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();

    }

}
