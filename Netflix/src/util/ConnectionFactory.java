package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexoes JDBC. Servidor, porta, usuario e senha ficam aqui, e os DAOs so chamam
 * getConnection(). A TelaConexao define esses valores e eles vao para o config.properties,
 * ao lado do Netflix.jar.
 *
 * O driver vai dentro do jar e o DriverManager o carrega sozinho (JDBC 4), sem Class.forName.
 */
public class ConnectionFactory {

    private static final String BANCO = "netflix";
    private static final String ARQUIVO_CONFIG = "config.properties";

    // Padrao de um MySQL instalado na propria maquina.
    private static String servidor = "localhost";
    private static int porta = 3306;
    private static String usuario = "root";
    private static String senha = "";

    // So metodos estaticos, entao nao precisa de instancia.
    private ConnectionFactory() {
    }

    /** Conexao com o banco netflix, usada pelos DAOs. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(montarUrl(BANCO), usuario, senha);
    }

    /** Conexao com o servidor, sem escolher banco. Serve para criar o banco netflix. */
    public static Connection getConnectionServidor() throws SQLException {
        return DriverManager.getConnection(montarUrl(""), usuario, senha);
    }

    // jdbc:mysql://servidor:porta/banco?parametros
    // useSSL=false e allowPublicKeyRetrieval=true evitam erros comuns em MySQL local.
    // connectionTimeZone=LOCAL usa o fuso do computador. Com o fuso fixo em America/Sao_Paulo,
    // 20:00 gravado aparecia como 23:00 em computadores de outro fuso.
    private static String montarUrl(String banco) {
        return "jdbc:mysql://" + servidor + ":" + porta + "/" + banco
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&connectionTimeZone=LOCAL&forceConnectionTimeZoneToSession=false";
    }

    /** Troca os dados de conexao. A TelaConexao chama via ConexaoController. */
    public static void configurar(String novoServidor, int novaPorta, String novoUsuario, String novaSenha) {
        servidor = novoServidor;
        porta = novaPorta;
        usuario = novoUsuario;
        senha = novaSenha;
    }

    /** Le o config.properties, se existir. */
    public static void carregarConfiguracao() {
        Properties config = new Properties();
        try (FileInputStream arquivo = new FileInputStream(ARQUIVO_CONFIG)) {
            config.load(arquivo);
            servidor = config.getProperty("servidor", servidor);
            porta = Integer.parseInt(config.getProperty("porta", String.valueOf(porta)));
            usuario = config.getProperty("usuario", usuario);
            senha = config.getProperty("senha", senha);
        } catch (IOException | NumberFormatException e) {
            // Arquivo ausente ou invalido: ficam os valores padrao.
        }
    }

    /** Grava os dados atuais no config.properties. */
    public static void salvarConfiguracao() throws IOException {
        Properties config = new Properties();
        config.setProperty("servidor", servidor);
        config.setProperty("porta", String.valueOf(porta));
        config.setProperty("usuario", usuario);
        config.setProperty("senha", senha);
        try (FileOutputStream arquivo = new FileOutputStream(ARQUIVO_CONFIG)) {
            config.store(arquivo, "Conexao com o MySQL (gerado pelo sistema Netflix)");
        }
    }

    public static String getServidor() { return servidor; }
    public static int getPorta() { return porta; }
    public static String getUsuario() { return usuario; }
    public static String getSenha() { return senha; }
}
