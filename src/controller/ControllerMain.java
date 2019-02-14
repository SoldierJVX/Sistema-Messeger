/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import view.Selecao;

/**
 *
 * @author SoldierJVX
 */
public class ControllerMain implements ActionListener {

    Selecao selecao;

    //Abrindo Janela de seleção para Servidor/Cliente
    public ControllerMain() {

        selecao = new Selecao();

        selecao.getBtnCliente().addActionListener(this);
        selecao.getBtnServidor().addActionListener(this);
        selecao.getBtnSair().addActionListener(this);

        selecao.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("Cliente")) {

            new ControllerCliente();
            selecao.dispose();
           

        } else if (e.getActionCommand().equals("Servidor")) {

            new ControllerServer();
            selecao.dispose();

        } else if (e.getActionCommand().equals("Sair")) {

            System.exit(0);

        }

    }

}
