/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import view.ConexaoCliente;
import view.Mensageiro;
import view.PainelArquivos;
import view.PainelCliente;

/**
 *
 * @author SoldierJVX
 */
class ControllerCliente extends Controller implements ActionListener {

    ControllerCliente este = this;
    String[] partesCliente;

    //Abrindo Janela para definir configurações para conectar ao Servidor
    public ControllerCliente() {

        view.put("conexaoCliente", new ConexaoCliente());

        ((ConexaoCliente) view.get("conexaoCliente")).getBtnConectar().addActionListener(this);
        ((ConexaoCliente) view.get("conexaoCliente")).getBtnConectar().addActionListener(this);
        ((ConexaoCliente) view.get("conexaoCliente")).getBtnSair().addActionListener(this);

        view.get("conexaoCliente").setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        System.out.println("Cliente: " + e.getActionCommand());

        if (e.getActionCommand().equals("Conectar")) {
            conectar();
        } else if (e.getActionCommand().equals("Sair")) {
            System.exit(0);
        } else if (e.getActionCommand().equals("Abrir Chat")) {
            abrirChat();
        } else if (e.getActionCommand().equals("Enviar Mensagem")) {
            enviarMensagem();
        } else if (e.getActionCommand().equals("Voltar Mensageiro")) {
            voltarMensageiro();
        } else if (e.getActionCommand().equals("Abrir Painel Arquivos")) {
            abrirPainelArquivos();
        } else if (e.getActionCommand().equals("Voltar Arquivos")) {
            voltarArquivos();
        } else if (e.getActionCommand().equals("Definir Diretório Arquivos")) {
            definirDiretorioArquivos();
        } else if (e.getActionCommand().equals("Enviar Arquivo")) {
            notificarEnvioArquivo();
        } else if (e.getActionCommand().equals("RECEBER ARQUIVO")) {
            actionReceberArquivo();
        } else if (e.getActionCommand().equals("Conversa por audio")) {
            ativarAudio();
        } else if (e.getActionCommand().equals("Compartilhar Tela")) {
            compartilharTela();
        } else if (e.getActionCommand().contains("RECEIVEARQ")) {
            enviarArquivo(e.getActionCommand().substring(11));
        } else if (e.getActionCommand().contains("ABRIRCHAT")) {
            abrirConexaoChat(e.getActionCommand().substring(10));
        } else if (e.getActionCommand().equals("CHAT OK")) {
            conectarChat();
        } else if (e.getActionCommand().equals("CHAT X")) {
            checarChat();
        } else if (e.getActionCommand().equals("CHECK MENSAGEIRO")) {
            checkMensageiro();
        } else if (e.getActionCommand().equals("MENSAGEIRO OK")) {
            mandarMensagem();
        } else if (e.getActionCommand().equals("MENSAGEIRO X")) {
            JOptionPane.showMessageDialog(null, "Servidor não abriu chat! Envie novamente quando cliente estiver com o chat aberto");
        }

    }

    public void notificarConexaoChat() {
        try {
            outputs.get("out").writeUTF("MENSAGEM");
        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void abrirConexaoChat(String acesso) {
        try {

            partesCliente = acesso.split("-");

            checarChat();

        } catch (Exception ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void conectarChat() {
        notificarConexaoChat();
        ((PainelCliente) view.get("painelCliente")).getBtnTela().setEnabled(true);
    }

    private void conectar() {

        if (sockets.get("conexaoServidor") == null) {

            try {
                ipDestino = ((ConexaoCliente) view.get("conexaoCliente")).getTxtIp().getText();
                int portaDestino = Integer.parseInt(((ConexaoCliente) view.get("conexaoCliente")).getTxtPorta().getText());

                sockets.put("conexaoServidor", new Socket(ipDestino, portaDestino));

                inputs.put("in", new DataInputStream(sockets.get("conexaoServidor").getInputStream()));
                outputs.put("out", new DataOutputStream(sockets.get("conexaoServidor").getOutputStream()));

                threadEscultarComandosServidor();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Socket servidorChat = new Socket(ipDestino, 30001);

                            sockets.put("mensagens", servidorChat);

                            inputs.put("inMens", new DataInputStream(sockets.get("mensagens").getInputStream()));
                            outputs.put("outMens", new DataOutputStream(sockets.get("mensagens").getOutputStream()));
                            threadReceberMensagensServidor();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        servers.put("recebeArqServer", new ServerSocket(30002));

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    sockets.put("recebeArq", servers.get("recebeArqServer").accept());

                                                    inputs.put("inArq", new DataInputStream(sockets.get("recebeArq").getInputStream()));

                                                } catch (SocketException ex2) {
                                                    System.exit(0);
                                                } catch (IOException ex) {
                                                    Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                            }
                                        }).start();

                                        sockets.put("envioArq", new Socket(ipDestino, 30002));

                                        outputs.put("outArq", new DataOutputStream(sockets.get("envioArq").getOutputStream()));

                                    } catch (SocketException ex2) {
                                        System.exit(0);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }).start();

                        } catch (SocketException ex2) {
                            System.exit(0);
                        } catch (IOException ex) {
                            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Compartilhamento
                            sockets.put("recebeComp", new Socket(ipDestino, 30003));
                            System.out.println("CLIENTE CONECTOU SOCKET TELA ");

                            outputs.put("outComp", new ObjectOutputStream(sockets.get("recebeComp").getOutputStream()));
                            inputs.put("inComp", new ObjectInputStream(sockets.get("recebeComp").getInputStream()));

                            ((PainelCliente) view.get("painelCliente")).getBtnTela().setEnabled(true);

                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }).start();

                if (view.get("painelCliente") == null) {

                    view.put("painelCliente", new PainelCliente());

                    view.get("conexaoCliente").dispose();
                    view.remove("conexaoCliente");

                    ((PainelCliente) view.get("painelCliente")).getBtnMensagem().addActionListener(this);
                    ((PainelCliente) view.get("painelCliente")).getBtnArquivo().addActionListener(this);
                    ((PainelCliente) view.get("painelCliente")).getBtnAudio().addActionListener(this);
                    ((PainelCliente) view.get("painelCliente")).getBtnTela().addActionListener(this);
                    ((PainelCliente) view.get("painelCliente")).getBtnSair().addActionListener(this);

                    view.get("painelCliente").setVisible(true);

                }

            } catch (SocketException ex2) {
                System.exit(0);
            } catch (IOException ex) {
                Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);

            } catch (NumberFormatException ex) {
                Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);

            }

        }
    }

    private void abrirChat() {
        view.get("painelCliente").setVisible(false);
        if (sockets.get("mensagens") == null) {
            notificarConexaoChat();
        }
        if (view.get("mensageiro") == null) {
            view.put("mensageiro", new Mensageiro());

            ((Mensageiro) view.get("mensageiro")).getBtnEnviar().addActionListener(this);
            ((Mensageiro) view.get("mensageiro")).getBtnVoltar().addActionListener(this);

        }

        mensageiro = true;
        view.get("mensageiro").setVisible(true);
    }

    private void enviarMensagem() {

        try {
            outputs.get("out").writeUTF("CHECK MENSAGEIRO");
        } catch (Exception ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void voltarMensageiro() {
        view.get("painelCliente").setVisible(true);

        view.get("mensageiro").setVisible(false);
    }

    private void abrirPainelArquivos() {
        view.get("painelCliente").setVisible(false);

        if (view.get("painelArquivos") == null) {

            view.put("painelArquivos", new PainelArquivos());

            ((PainelArquivos) view.get("painelArquivos")).getBtnEnviar().addActionListener(this);
            ((PainelArquivos) view.get("painelArquivos")).getBtnVoltar().addActionListener(this);
            ((PainelArquivos) view.get("painelArquivos")).getBtnDiretorio().addActionListener(this);
            ((PainelArquivos) view.get("painelArquivos")).getTxtDiretorioSalvar().addActionListener(this);

        }

        view.get("painelArquivos").setVisible(true);
    }

    private void voltarArquivos() {
        view.get("painelCliente").setVisible(true);

        view.get("painelArquivos").setVisible(false);
    }

    private void threadEscultarComandosServidor() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String comando;

                while (true) {
                    try {
                        comando = inputs.get("in").readUTF();

                        este.actionPerformed(new ActionEvent(comando, 0, comando));

                    } catch (SocketException ex2) {
                        System.exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }
        }).start();
    }

    private void threadReceberMensagensServidor() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    while (true) {

                        String mensagem = inputs.get("inMens").readUTF();

                        ((Mensageiro) view.get("mensageiro")).getTxtChat().append("Servidor: " + mensagem + "\n");

                    }

                } catch (SocketException ex2) {
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void checarChat() {
        try {
            outputs.get("out").writeUTF("CHECK CHAT");
        } catch (Exception ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mandarMensagem() {
        try {

            if (outputs.get("outMens") != null) {

                String mensagemAEnviar = ((Mensageiro) view.get("mensageiro")).getTxtMensagem().getText();
                ((Mensageiro) view.get("mensageiro")).getTxtMensagem().setText("");

                ((Mensageiro) view.get("mensageiro")).getTxtChat().append("Cliente: " + mensagemAEnviar + "\n");

                outputs.get("outMens").writeUTF(mensagemAEnviar);
            }

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkMensageiro() {
        try {
            if (mensageiro) {
                outputs.get("out").writeUTF("MENSAGEIRO OK");
            } else {
                outputs.get("out").writeUTF("MENSAGEIRO X");
            }
        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
