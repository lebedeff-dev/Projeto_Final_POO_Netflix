package controller;

import java.io.IOException;
import java.sql.SQLException;

import util.ConnectionFactory;
import util.Conversor;
import util.CriadorBanco;

/**
 * Controller da conexao com o MySQL, usado pela TelaConexao e pelo menu. Recebe servidor,
 * usuario e senha, cria o banco com os dados de exemplo e salva a configuracao no config.properties.
 */
public class ConexaoController {

    // Le o config.properties so na primeira vez. O menu cria outro ConexaoController (para
    // "Restaurar dados"), e sem esta trava ele leria o arquivo de novo e poderia trocar a senha
    // recem-digitada por uma antiga, se o arquivo nao tiver sido atualizado.
    private static boolean configuracaoCarregada = false;

    // Aviso do ultimo conectar(), por exemplo quando a configuracao nao foi salva. Nao e erro.
    private String aviso;

    /** Na primeira vez, le o config.properties (se existir) para preencher a tela. */
    public ConexaoController() {
        if (!configuracaoCarregada) {
            ConnectionFactory.carregarConfiguracao();
            configuracaoCarregada = true;
        }
    }

    /** Aviso do ultimo conectar() bem-sucedido, ou null. */
    public String getAviso() { return aviso; }

    public String servidorSalvo() { return ConnectionFactory.getServidor(); }
    public String portaSalva() { return String.valueOf(ConnectionFactory.getPorta()); }
    public String usuarioSalvo() { return ConnectionFactory.getUsuario(); }
    public String senhaSalva() { return ConnectionFactory.getSenha(); }

    /** Devolve null se conectou, ou a mensagem de erro. */
    public String conectar(String servidor, String porta, String usuario, String senha) {
        try {
            ConnectionFactory.configurar(
                    Conversor.obrigatorio(servidor, "Servidor"),
                    Conversor.paraInt(porta, "Porta"),
                    Conversor.obrigatorio(usuario, "Usuário"),
                    senha);
            CriadorBanco.criarSeNaoExistir(); // so cria se o banco nao existir
            aviso = null;
            try {
                ConnectionFactory.salvarConfiguracao();
            } catch (IOException e) {
                // Pasta protegida ou somente leitura: nao barra o login, so avisa que a senha
                // sera pedida de novo.
                aviso = "Conectado, mas não foi possível salvar a configuração (" + e.getMessage()
                        + ").\nNa próxima vez será preciso digitar a senha de novo.";
            }
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Não foi possível conectar ao MySQL. Confira se ele está ligado e se o usuário/senha "
                    + "estão certos.\n\nDetalhe: " + e.getMessage();
        } catch (IOException e) {
            return "Erro ao ler o script do banco ou salvar a configuração: " + e.getMessage();
        }
    }

    /** Apaga o banco e cria de novo com os dados do script. */
    public String restaurarDadosExemplo() {
        try {
            CriadorBanco.recriarBanco();
            return "Banco recriado com os dados de exemplo!";
        } catch (SQLException | IOException e) {
            return "Erro ao recriar o banco: " + e.getMessage();
        }
    }
}
