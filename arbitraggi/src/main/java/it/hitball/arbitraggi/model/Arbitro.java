package it.hitball.arbitraggi.model;

import java.util.List;

public class Arbitro {
	
	private String cognome;
	private String nome;
	private String maxSerie;
	private String dispSettimanale;
	private List<String> squadreDaEvitare;
	
	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getMaxSerie() {
		return maxSerie;
	}

	public void setMaxSerie(String maxSerie) {
		this.maxSerie = maxSerie;
	}

	public String getDispSettimanale() {
		return dispSettimanale;
	}

	public void setDispSettimanale(String dispSettimanale) {
		this.dispSettimanale = dispSettimanale;
	}

	public List<String> getSquadreDaEvitare() {
		return squadreDaEvitare;
	}

	public void setSquadreDaEvitare(List<String> squadreDaEvitare) {
		this.squadreDaEvitare = squadreDaEvitare;
	}

}
