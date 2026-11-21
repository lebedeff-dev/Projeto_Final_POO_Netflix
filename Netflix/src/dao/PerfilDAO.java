package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Perfil;
import util.ConnectionFactory;

/** DAO de Perfil, no molde do PlanoDAO. A FK id_conta liga o perfil a sua conta. */
public class PerfilDAO {

    public void inserir(Perfil p) throws SQLException {
        String sql = "INSERT INTO perfil (id_conta, nome, avatar, data_criacao, perfil_infantil, pin) "
                   + "VALUES (?, ?, ?, CURDATE(), ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getIdConta());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getAvatar());
            ps.setBoolean(4, p.isPerfilInfantil());
            ps.setString(5, p.getPin()); // PIN e opcional
            ps.executeUpdate();
        }
    }

    public List<Perfil> listar() throws SQLException {
        // INNER JOIN com conta para trazer o e-mail da conta dona do perfil.
        String sql = "SELECT p.id_perfil, p.id_conta, p.nome, p.avatar, p.perfil_infantil, "
                   + "p.pin, c.email AS email_conta "
                   + "FROM perfil p INNER JOIN conta c ON c.id_conta = p.id_conta "
                   + "ORDER BY p.id_perfil";
        List<Perfil> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Perfil p = new Perfil();
                p.setIdPerfil(rs.getInt("id_perfil"));
                p.setIdConta(rs.getInt("id_conta"));
                p.setNome(rs.getString("nome"));
                p.setAvatar(rs.getString("avatar"));
                p.setPerfilInfantil(rs.getBoolean("perfil_infantil"));
                p.setPin(rs.getString("pin"));
                p.setEmailConta(rs.getString("email_conta")); // coluna que veio do JOIN
                lista.add(p);
            }
        }
        return lista;
    }

    public boolean atualizar(Perfil p) throws SQLException {
        String sql = "UPDATE perfil SET id_conta = ?, nome = ?, avatar = ?, "
                   + "perfil_infantil = ?, pin = ? WHERE id_perfil = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getIdConta());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getAvatar());
            ps.setBoolean(4, p.isPerfilInfantil());
            ps.setString(5, p.getPin());
            ps.setInt(6, p.getIdPerfil());
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** O historico do perfil e apagado junto (ON DELETE CASCADE). */
    public boolean eliminar(int idPerfil) throws SQLException {
        String sql = "DELETE FROM perfil WHERE id_perfil = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPerfil);
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }
}
