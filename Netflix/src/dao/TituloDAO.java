package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import model.Titulo;
import util.ConnectionFactory;

/** DAO de Titulo, no molde do PlanoDAO. A duracao_minutos pode ser NULL (series). */
public class TituloDAO {

    public void inserir(Titulo t) throws SQLException {
        String sql = "INSERT INTO titulo (nome, sinopse, tipo, ano_lancamento, classificacao_indicativa, "
                   + "duracao_minutos, idioma_original, data_adicao_catalogo, disponivel) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE(), ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            preencher(ps, t);
            ps.executeUpdate();
        }
    }

    public List<Titulo> listar() throws SQLException {
        String sql = "SELECT id_titulo, nome, sinopse, tipo, ano_lancamento, classificacao_indicativa, "
                   + "duracao_minutos, idioma_original, disponivel "
                   + "FROM titulo ORDER BY id_titulo";
        List<Titulo> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    /** Devolve o titulo do id, ou null se nao existir. */
    public Titulo buscarPorId(int idTitulo) throws SQLException {
        String sql = "SELECT id_titulo, nome, sinopse, tipo, ano_lancamento, classificacao_indicativa, "
                   + "duracao_minutos, idioma_original, disponivel "
                   + "FROM titulo WHERE id_titulo = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTitulo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    /** Converte a linha atual do ResultSet em Titulo. */
    private Titulo montar(ResultSet rs) throws SQLException {
        Titulo t = new Titulo();
        t.setIdTitulo(rs.getInt("id_titulo"));
        t.setNome(rs.getString("nome"));
        t.setSinopse(rs.getString("sinopse"));
        t.setTipo(rs.getString("tipo"));
        t.setAnoLancamento(rs.getInt("ano_lancamento"));
        t.setClassificacaoIndicativa(rs.getString("classificacao_indicativa"));

        // getInt devolve 0 para NULL, entao wasNull() diz se o valor era NULL de verdade.
        int duracao = rs.getInt("duracao_minutos");
        t.setDuracaoMinutos(rs.wasNull() ? null : duracao);

        t.setIdiomaOriginal(rs.getString("idioma_original"));
        t.setDisponivel(rs.getBoolean("disponivel"));
        return t;
    }

    public boolean atualizar(Titulo t) throws SQLException {
        String sql = "UPDATE titulo SET nome = ?, sinopse = ?, tipo = ?, ano_lancamento = ?, "
                   + "classificacao_indicativa = ?, duracao_minutos = ?, idioma_original = ?, "
                   + "disponivel = ? WHERE id_titulo = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            preencher(ps, t);
            ps.setInt(9, t.getIdTitulo());
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** O historico do titulo e apagado junto (ON DELETE CASCADE). */
    public boolean eliminar(int idTitulo) throws SQLException {
        String sql = "DELETE FROM titulo WHERE id_titulo = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTitulo);
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** Preenche os 8 primeiros "?", iguais no INSERT e no UPDATE. */
    private void preencher(PreparedStatement ps, Titulo t) throws SQLException {
        ps.setString(1, t.getNome());
        ps.setString(2, t.getSinopse());
        ps.setString(3, t.getTipo());
        ps.setInt(4, t.getAnoLancamento());
        ps.setString(5, t.getClassificacaoIndicativa());

        // setInt com Integer nulo daria NullPointerException, entao usa setNull com o tipo da coluna.
        if (t.getDuracaoMinutos() == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, t.getDuracaoMinutos());
        }

        ps.setString(7, t.getIdiomaOriginal());
        ps.setBoolean(8, t.isDisponivel());
    }
}
