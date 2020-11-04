package free.example.com.hearme.model;

public enum Teste {
	TESTA("Testa"), NAOTESTA("Não Testa"), INDEFINIDO("Não informado!");

	// var para descrever
	private String descricao;

	// construtor privado
	private Teste(String des) {
		// populando a descri��o
		this.descricao = des;
	}

	@Override
	public String toString() {

		return this.descricao;
	}
}
