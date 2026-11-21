package controller;

import java.sql.SQLException;
import java.util.List;

import dao.TituloDAO;
import model.Titulo;
import util.Conversor;

/** Controller de Titulo, no padrao do PlanoController. */
public class TituloController {

    private final TituloDAO dao = new TituloDAO();

    public List<Titulo> listar() {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar títulos: " + e.getMessage(), e);
        }
    }

    public String inserir(String nome, String sinopse, String tipo, String ano, String classificacao,
                          String duracao, String idioma, String disponivel) {
        try {
            dao.inserir(montar(nome, sinopse, tipo, ano, classificacao, duracao, idioma, disponivel));
            return "Título inserido com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public String atualizar(String id, String nome, String sinopse, String tipo, String ano, String classificacao,
                            String duracao, String idioma, String disponivel) {
        try {
            int idTitulo = Conversor.paraId(id);
            Titulo t = montar(nome, sinopse, tipo, ano, classificacao, duracao, idioma, disponivel);
            t.setIdTitulo(idTitulo);
            if (!dao.atualizar(t)) {
                return Conversor.REGISTRO_NAO_ENCONTRADO;
            }
            return "Título atualizado com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public String eliminar(String id) {
        try {
            if (!dao.eliminar(Conversor.paraId(id))) {
                return Conversor.REGISTRO_NAO_ENCONTRADO;
            }
            return "Título removido com sucesso (o histórico dele também foi apagado).";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    private Titulo montar(String nome, String sinopse, String tipo, String ano, String classificacao,
                          String duracao, String idioma, String disponivel) {
        return new Titulo(
                Conversor.obrigatorio(nome, "Nome"),
                Conversor.vazioParaNulo(sinopse),
                tipo,
                Conversor.paraInt(ano, "Ano lançamento"),
                classificacao,
                Conversor.paraIntOuNulo(duracao, "Duração (min)"), // vazio vira null (series)
                Conversor.vazioParaNulo(idioma),
                Conversor.paraBoolean(disponivel));
    }
}
