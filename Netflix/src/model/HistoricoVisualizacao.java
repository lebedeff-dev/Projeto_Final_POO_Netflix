package model;

import java.time.LocalDateTime;

/**
 * Uma sessao assistida (tabela historico_visualizacao). Liga Perfil e Titulo (N:N) com duas FKs
 * e guarda quando, ate que minuto e em qual aparelho.
 */
public class HistoricoVisualizacao {

    private int idVisualizacao;
    private int idPerfil;                         // FK -> perfil.id_perfil
    private int idTitulo;                         // FK -> titulo.id_titulo
    private LocalDateTime dataInicio;             // DATETIME
    private LocalDateTime dataUltimaVisualizacao; // DATETIME
    private int minutoParada;
    private boolean concluido;                    // TINYINT(1)
    private String dispositivo;                   // ENUM('TV','Celular','Notebook','Tablet','Console')

    // So para exibir, nao existem na tabela. O INNER JOIN preenche e o INSERT/UPDATE ignora.
    private String nomePerfil;
    private String nomeTitulo;
    private String emailConta;
    private String nomePlano;

    public HistoricoVisualizacao() {
    }

    public HistoricoVisualizacao(int idPerfil, int idTitulo, int minutoParada,
                                 boolean concluido, String dispositivo) {
        this.idPerfil = idPerfil;
        this.idTitulo = idTitulo;
        this.minutoParada = minutoParada;
        this.concluido = concluido;
        this.dispositivo = dispositivo;
    }

    public int getIdVisualizacao() { return idVisualizacao; }
    public void setIdVisualizacao(int idVisualizacao) { this.idVisualizacao = idVisualizacao; }

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }

    public int getIdTitulo() { return idTitulo; }
    public void setIdTitulo(int idTitulo) { this.idTitulo = idTitulo; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataUltimaVisualizacao() { return dataUltimaVisualizacao; }
    public void setDataUltimaVisualizacao(LocalDateTime dataUltimaVisualizacao) { this.dataUltimaVisualizacao = dataUltimaVisualizacao; }

    public int getMinutoParada() { return minutoParada; }
    public void setMinutoParada(int minutoParada) { this.minutoParada = minutoParada; }

    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }

    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }

    public String getNomePerfil() { return nomePerfil; }
    public void setNomePerfil(String nomePerfil) { this.nomePerfil = nomePerfil; }

    public String getNomeTitulo() { return nomeTitulo; }
    public void setNomeTitulo(String nomeTitulo) { this.nomeTitulo = nomeTitulo; }

    public String getEmailConta() { return emailConta; }
    public void setEmailConta(String emailConta) { this.emailConta = emailConta; }

    public String getNomePlano() { return nomePlano; }
    public void setNomePlano(String nomePlano) { this.nomePlano = nomePlano; }
}
