import javax.swing.SwingUtilities;

import view.TelaConexao;

/** Ponto de entrada. Abre a tela de conexao, que abre o menu depois de conectar. */
public class Main {

    public static void main(String[] args) {
        // As telas do Swing precisam ser criadas na thread de eventos.
        SwingUtilities.invokeLater(() -> new TelaConexao().setVisible(true));
    }
}
