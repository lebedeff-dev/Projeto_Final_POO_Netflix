package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Cria o banco netflix rodando o sql/netflix.sql, que vai dentro do jar. Precisa so do MySQL
 * ligado, sem Workbench. O mesmo script esta em Netflix/sql para abrir no Workbench.
 */
public class CriadorBanco {

    private CriadorBanco() {
    }

    /**
     * Cria o banco se ele nao existe ou esta sem tabelas. Um banco que ja tem tabelas e reaproveitado
     * e nunca apagado aqui. Quem apaga e o botao Restaurar dados.
     */
    public static void criarSeNaoExistir() throws SQLException, IOException {
        if (!bancoExiste() || bancoSemTabelas()) {
            recriarBanco();
        }
    }

    /** Diz se o banco netflix nao tem nenhuma tabela. */
    public static boolean bancoSemTabelas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'netflix'";
        try (Connection con = ConnectionFactory.getConnectionServidor();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    /** Diz se o banco netflix existe. */
    public static boolean bancoExiste() throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'netflix'";
        try (Connection con = ConnectionFactory.getConnectionServidor();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    /** Apaga e cria o banco de novo com os dados de exemplo. O script comeca com DROP DATABASE. */
    public static void recriarBanco() throws SQLException, IOException {
        String script = lerScript();

        // O JDBC executa um comando por vez, entao o script e cortado em cada ";".
        // Statement basta porque o script nao tem valores digitados pelo usuario.
        try (Connection con = ConnectionFactory.getConnectionServidor();
             Statement st = con.createStatement()) {
            for (String comando : script.split(";")) {
                if (!comando.trim().isEmpty()) {
                    st.execute(comando);
                }
            }
        }
    }

    /** Le o netflix.sql de dentro do jar e remove os comentarios "--". */
    private static String lerScript() throws IOException {
        try (InputStream entrada = CriadorBanco.class.getResourceAsStream("/netflix.sql")) {
            if (entrada == null) {
                throw new IOException("Arquivo netflix.sql nao encontrado dentro do sistema.");
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            entrada.transferTo(bytes);
            String texto = bytes.toString(StandardCharsets.UTF_8.name());

            StringBuilder semComentarios = new StringBuilder();
            for (String linha : texto.split("\n")) {
                int comentario = linha.indexOf("--");
                semComentarios.append(comentario >= 0 ? linha.substring(0, comentario) : linha).append('\n');
            }
            return semComentarios.toString();
        }
    }
}
