package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexoes JDBC.
 *
 * Por que uma classe so para isso?
 *  - Os dados de conexao (servidor, porta, usuario, senha) ficam em UM unico lugar.
 *    Os DAOs so precisam chamar ConnectionFactory.getConnection().
 *  - Na versao principal esses dados NAO ficam mais fixos no codigo: a TelaConexao pergunta ao
 *    usuario e eles sao salvos no arquivo config.properties (ao lado do Netflix.jar).
 *    Assim ninguem precisa editar o codigo e recompilar para mudar a senha.
 *
 * O driver (mysql-connector-j) ja vem dentro do Netflix.jar. Desde o JDBC 4 ele e
 * carregado sozinho pelo DriverManager, por isso nao precisamos de Class.forName.
 */
public class ConnectionFactory {

    private static final String BANCO = "netflix";
    private static final String ARQUIVO_CONFIG = "config.properties";

    // Valores padrao (os mais comuns em um MySQL instalado na propria maquina).
    private static String servidor = "localhost";
    private static int porta = 3306;
    private static String usuario = "root";
    private static String senha = "";

    // Construtor privado: a classe so tem metodos estaticos, ninguem precisa dar "new".
    private ConnectionFactory() {
    }

    /** Conexao com o banco "netflix". Usada por todos os DAOs. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(montarUrl(BANCO), usuario, senha);
    }

    /** Conexao com o servidor MySQL sem escolher banco (usada para criar o banco "netflix"). */
    public static Connection getConnectionServidor() throws SQLException {
        return DriverManager.getConnection(montarUrl(""), usuario, senha);
    }

    // jdbc:mysql://servidor:porta/banco?parametros
    // useSSL=false e allowPublicKeyRetrieval=true evitam erros comuns em MySQL local;
    // connectionTimeZone=LOCAL: o driver usa o fuso do proprio computador para converter datas. Antes
    // estava fixo em America/Sao_Paulo e, num computador com outro fuso, o horario lido aparecia
    // deslocado (ex.: 20:00 gravado aparecia como 23:00). Com LOCAL o horario digitado e o mostrado
    // sao sempre iguais, em qualquer computador.
    private static String montarUrl(String banco) {
        return "jdbc:mysql://" + servidor + ":" + porta + "/" + banco
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&connectionTimeZone=LOCAL&forceConnectionTimeZoneToSession=false";
    }

    /** Troca os dados de conexao (chamado pela tela de conexao, via ConexaoController). */
    public static void configurar(String novoServidor, int novaPorta, String novoUsuario, String novaSenha) {
        servidor = novoServidor;
        porta = novaPorta;
        usuario = novoUsuario;
        senha = novaSenha;
    }

    /** Le o config.properties, se existir (na primeira execucao ele ainda nao existe). */
    public static void carregarConfiguracao() {
        Properties config = new Properties();
        try (FileInputStream arquivo = new FileInputStream(ARQUIVO_CONFIG)) {
            config.load(arquivo);
            servidor = config.getProperty("servidor", servidor);
            porta = Integer.parseInt(config.getProperty("porta", String.valueOf(porta)));
            usuario = config.getProperty("usuario", usuario);
            senha = config.getProperty("senha", senha);
        } catch (IOException | NumberFormatException e) {
            // Arquivo inexistente ou invalido: ficam os valores padrao.
        }
    }

    /** Grava os dados atuais no config.properties para a proxima execucao. */
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
