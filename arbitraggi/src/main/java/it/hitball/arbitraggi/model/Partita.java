package it.hitball.arbitraggi.model;

import java.util.Date;

public class Partita {
	
	private Date data;
	private String serie;
	private String squadraA;
	private String squadraB;
	private Arbitro primoArbitro;
	private Arbitro secondoArbitro;
	private Arbitro refertista;

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getSquadraA() {
		return squadraA;
	}

	public void setSquadraA(String squadraA) {
		this.squadraA = squadraA;
	}

	public String getSquadraB() {
		return squadraB;
	}

	public void setSquadraB(String squadraB) {
		this.squadraB = squadraB;
	}

	public Arbitro getPrimoArbitro() {
		return primoArbitro;
	}

	public void setPrimoArbitro(Arbitro primoArbitro) {
		this.primoArbitro = primoArbitro;
	}

	public Arbitro getSecondoArbitro() {
		return secondoArbitro;
	}

	public void setSecondoArbitro(Arbitro secondoArbitro) {
		this.secondoArbitro = secondoArbitro;
	}

	public Arbitro getRefertista() {
		return refertista;
	}

	public void setRefertista(Arbitro refertista) {
		this.refertista = refertista;
	}

}
