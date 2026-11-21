package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;

/**
 * Converte o texto digitado em int, BigDecimal, LocalDate etc. Texto invalido lanca
 * IllegalArgumentException com a mensagem pronta para a tela, e o Controller devolve essa mensagem.
 */
public class Conversor {

    // Formato brasileiro. STRICT recusa datas que nao existem, como 31/02/2026.
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    // Formato de data e hora mostrado nas tabelas e no relatorio.
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Mensagem quando o UPDATE ou DELETE nao acha o id, por exemplo se outra janela removeu o registro. */
    public static final String REGISTRO_NAO_ENCONTRADO =
            "Registro não encontrado (ele pode ter sido removido em outra janela). A tabela foi atualizada.";

    private Conversor() {
    }

    /** Campo obrigatorio, nao pode ficar vazio. */
    public static String obrigatorio(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo '" + campo + "' é obrigatório.");
        }
        return texto.trim();
    }

    /** Campo opcional: texto vazio vira null, que o DAO grava como NULL. */
    public static String vazioParaNulo(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? null : texto.trim();
    }

    public static int paraInt(String texto, String campo) {
        try {
            return Integer.parseInt(obrigatorio(texto, campo));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser um número inteiro.");
        }
    }

    /** Inteiro opcional: vazio vira null. */
    public static Integer paraIntOuNulo(String texto, String campo) {
        return vazioParaNulo(texto) == null ? null : paraInt(texto, campo);
    }

    public static BigDecimal paraDecimal(String texto, String campo) {
        try {
            return new BigDecimal(obrigatorio(texto, campo).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser um número (ex.: 39.90).");
        }
    }

    public static LocalDate paraData(String texto, String campo) {
        try {
            return LocalDate.parse(obrigatorio(texto, campo), DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser uma data válida no formato dd/mm/aaaa (ex.: 31/12/2026).");
        }
    }

    /** Combo "Sim"/"Nao" -> boolean. */
    public static boolean paraBoolean(String texto) {
        return "Sim".equals(texto);
    }

    public static String simNao(boolean valor) {
        return valor ? "Sim" : "Nao";
    }

    /**
     * Monta o texto "3 - Premium" dos combos de FK e das colunas da tabela. Com o mesmo formato
     * nos dois, clicar numa linha ja seleciona o item certo no combo.
     */
    public static String opcao(int id, String nome) {
        return id + " - " + nome;
    }

    /** Pega o id de um texto como "3 - Premium". */
    public static int extrairId(String opcao, String campo) {
        if (opcao == null || opcao.trim().isEmpty()) {
            throw new IllegalArgumentException("Selecione um valor em '" + campo + "'.");
        }
        return paraInt(opcao.split(" - ")[0], campo);
    }

    /** Id da linha selecionada. Vazio quer dizer que nada foi selecionado. */
    public static int paraId(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Selecione um registro na tabela primeiro.");
        }
        return paraInt(texto, "ID");
    }

    public static String formatarDataHora(LocalDateTime dataHora) {
        return dataHora.format(DATA_HORA);
    }

    public static String hoje() {
        return LocalDate.now().format(DATA);
    }

}
