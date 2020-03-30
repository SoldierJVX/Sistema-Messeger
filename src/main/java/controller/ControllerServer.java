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
import view.ConexaoServidor;
import view.Mensageiro;
import view.PainelArquivos;
import view.PainelServidor;

/**
 *
 * @author SoldierJVX
 */
class ControllerServer extends Controller implements ActionListener {

    ControllerServer este = this;
    boolean chat = false;

    public ControllerServer() {
        try {

            servers.put("servidor", new ServerSocket(30000));
            view.put("conexaoServidor", new ConexaoServidor());

            ((ConexaoServidor) view.get("conexaoServidor")).getBtnSair().addActionListener(this);

            view.get("conexaoServidor").setVisible(true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sockets.put("conexaoCliente", servers.get("servidor").accept());

                        ipDestino = sockets.get("conexaoCliente").getInetAddress().toString().replace("/", "");

                        inputs.put("in", new DataInputStream(sockets.get("conexaoCliente").getInputStream()));
                        outputs.put("out", new DataOutputStream(sockets.get("conexaoCliente").getOutputStream()));

                        escultarComandosCliente();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    servers.put("mensagensServer", new ServerSocket(30001));

                                    Socket clienteChat = servers.get("mensagensServer").accept();

                                    sockets.put("mensagens", clienteChat);

                                    inputs.put("inMens", new DataInputStream(sockets.get("mensagens").getInputStream()));
                                    outputs.put("outMens", new DataOutputStream(sockets.get("mensagens").getOutputStream()));

                                    threadReceberMensagensCliente();

                                    //Envio Arquivo
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
                                    Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }).start();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Compartilhamento
                                    servers.put("compartilhamento", new ServerSocket(30003));

                                    sockets.put("recebeComp", servers.get("compartilhamento").accept());

                                    inputs.put("inComp", new ObjectInputStream(sockets.get("recebeComp").getInputStream()));
                                    outputs.put("outComp", new ObjectOutputStream(sockets.get("recebeComp").getOutputStream()));

                                    ((PainelServidor) view.get("painelServidor")).getBtnTela().setEnabled(true);

                                } catch (SocketException ex2) {
                                    System.exit(0);
                                } catch (IOException ex) {
                                    Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }).start();

                        este.actionPerformed(new ActionEvent(este, 0, "MostrarPainel"));

                    } catch (SocketException ex2) {
                        System.exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                private void escultarComandosCliente() {
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
            }).start();

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        System.out.println("Servidor: " + e.getActionCommand());

        if (e.getActionCommand().equals("Sair")) {
            System.exit(0);
        } else if (e.getActionCommand().equals("MostrarPainel")) {
            mostrarPainel();
        } else if (e.getActionCommand().equals("Abrir Chat")) {
            abrirChat();
        } else if (e.getActionCommand().equals("Enviar Mensagem")) {
            enviarMensagem();
        } else if (e.getActionCommand().equals("Voltar Mensageiro")) {
            voltarMensageiro();
        } else if (e.getActionCommand().equals("MENSAGEM")) {
            actionMensagem();
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
        } else if (e.getActionCommand().contains("RECEIVEARQ")) {
            enviarArquivo(e.getActionCommand().substring(11));
        } else if (e.getActionCommand().contains("CHECK CHAT")) {
            checarChat();
        } else if (e.getActionCommand().equals("Compartilhar Tela")) {
            compartilharTela();
        } else if (e.getActionCommand().equals("CHECK MENSAGEIRO")) {
            checkMensageiro();
        } else if (e.getActionCommand().equals("MENSAGEIRO OK")) {
            mandarMensagem();
        } else if (e.getActionCommand().equals("MENSAGEIRO X")) {
            JOptionPane.showMessageDialog(null, "Cliente não abriu chat! Envie novamente quando cliente estiver com o chat aberto");
        }
    }

    private void mostrarPainel() {
        view.put("painelServidor", new PainelServidor());

        ((PainelServidor) view.get("painelServidor")).getBtnMensagem().addActionListener(this);
        ((PainelServidor) view.get("painelServidor")).getBtnArquivo().addActionListener(this);
        ((PainelServidor) view.get("painelServidor")).getBtnAudio().addActionListener(this);
        ((PainelServidor) view.get("painelServidor")).getBtnTela().addActionListener(this);
        ((PainelServidor) view.get("painelServidor")).getBtnSair().addActionListener(this);

        view.get("conexaoServidor").dispose();
        view.remove("conexaoServidor");

        view.get("painelServidor").setVisible(true);
    }

    private void abrirChat() {
        view.get("painelServidor").setVisible(false);

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
        view.get("painelServidor").setVisible(true);

        view.get("mensageiro").setVisible(false);
    }

    private void actionMensagem() {
        if (servers.get("mensagensServer") == null) {
            abrirConexaoChat();
        }
    }

    public void abrirConexaoChat() {

        try {

            String mensagem = "ABRIRCHAT " + ipDestino + "-" + servers.get("mensagensServer").getLocalPort();
            outputs.get("out").writeUTF(mensagem);

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void abrirPainelArquivos() {
        view.get("painelServidor").setVisible(false);

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
        view.get("painelServidor").setVisible(true);

        view.get("painelArquivos").setVisible(false);
    }

    private void threadReceberMensagensCliente() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String mensagem = inputs.get("inMens").readUTF();

                        ((Mensageiro) view.get("mensageiro")).getTxtChat().append("Cliente: " + mensagem + "\n");

                    } catch (SocketException ex2) {
                        System.exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

    }

    private void checarChat() {

        try {
            if (chat) {
                outputs.get("out").writeUTF("CHAT OK");
            } else {
                outputs.get("out").writeUTF("CHAT X");
            }

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerServer.class.getName()).log(Level.SEVERE, null, ex);
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

    private void mandarMensagem() {
        try {

            if (outputs.get("outMens") != null) {

                String mensagemAEnviar = ((Mensageiro) view.get("mensageiro")).getTxtMensagem().getText();
                ((Mensageiro) view.get("mensageiro")).getTxtMensagem().setText("");

                ((Mensageiro) view.get("mensageiro")).getTxtChat().append("Servidor: " + mensagemAEnviar + "\n");

                outputs.get("outMens").writeUTF(mensagemAEnviar);
            } else {
                JOptionPane.showMessageDialog(null, "Cliente não abriu chat! Envie novamente quando cliente estiver com o chat aberto");
            }

        } catch (SocketException ex2) {
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ControllerCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
