package model;


/** Item do catalogo (tabela titulo). Nao tem FK; quem aponta para ele e o historico. */
public class Titulo {

    private int idTitulo;
    private String nome;
    private String sinopse;
    private String tipo;                     // ENUM('Filme','Serie','Documentario','Stand-up')
    private int anoLancamento;               // SMALLINT
    private String classificacaoIndicativa;  // ENUM('L','10','12','14','16','18')
    private Integer duracaoMinutos;          // Integer porque serie nao tem duracao (NULL)
    private String idiomaOriginal;
    private boolean disponivel;              // TINYINT(1)

    public Titulo() {
    }

    public Titulo(String nome, String sinopse, String tipo, int anoLancamento,
                  String classificacaoIndicativa, Integer duracaoMinutos, String idiomaOriginal,
                  boolean disponivel) {
        this.nome = nome;
        this.sinopse = sinopse;
        this.tipo = tipo;
        this.anoLancamento = anoLancamento;
        this.classificacaoIndicativa = classificacaoIndicativa;
        this.duracaoMinutos = duracaoMinutos;
        this.idiomaOriginal = idiomaOriginal;
        this.disponivel = disponivel;
    }

    public int getIdTitulo() { return idTitulo; }
    public void setIdTitulo(int idTitulo) { this.idTitulo = idTitulo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(int anoLancamento) { this.anoLancamento = anoLancamento; }

    public String getClassificacaoIndicativa() { return classificacaoIndicativa; }
    public void setClassificacaoIndicativa(String classificacaoIndicativa) { this.classificacaoIndicativa = classificacaoIndicativa; }

    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public String getIdiomaOriginal() { return idiomaOriginal; }
    public void setIdiomaOriginal(String idiomaOriginal) { this.idiomaOriginal = idiomaOriginal; }


    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
}
