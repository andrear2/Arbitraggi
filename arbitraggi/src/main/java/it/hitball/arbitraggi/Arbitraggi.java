package it.hitball.arbitraggi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import it.hitball.arbitraggi.model.Arbitro;
import it.hitball.arbitraggi.model.Partita;

public class Arbitraggi {
	
	private static final String ARBITRI_FILE_NAME = "Arbitri.xlsx";
	private static final String CALENDARIO_FILE_NAME = "Calendario.xlsx";
	
	// Colonne file arbitri
	private static final int COGNOME_INDEX = 0;
	private static final int NOME_INDEX = 1;
	private static final int MAX_SERIE_INDEX = 2;
	private static final int DISP_SETTIMANALE_INDEX = 3;
	private static final int SQUADRE_DA_EVITARE_INDEX = 4;
	
	// Colonne file calendario
	private static final int DATA_INDEX = 0;
	private static final int SERIE_INDEX = 1;
	private static final int SQUADRA_A_INDEX = 2;
	private static final int SQUADRA_B_INDEX = 3;
	private static final int PRIMO_ARBITRO_INDEX = 4;
	private static final int SECONDO_ARBITRO_INDEX = 5;
	private static final int REFERTISTA_INDEX = 6;
	
	public static void main(String[] args) {
		List<Arbitro> arbitri = leggiArbitri();
		
		if (arbitri.size() > 0) {
			creaArbitraggi(arbitri);
		}
	}

	private static List<Arbitro> leggiArbitri() {
		List<Arbitro> arbitri = new ArrayList<>();
		
		try {
			Workbook workbook = WorkbookFactory.create(new File(ARBITRI_FILE_NAME));
			Sheet sheet = workbook.getSheetAt(0);
			
			Iterator<Row> righe = sheet.rowIterator();
			while (righe.hasNext()) {
				Row riga = righe.next();
				// salto la prima riga (intestazione)
				if (riga.getRowNum() == 0) {
					continue;
				}
				
				Arbitro arbitro = new Arbitro();
				Iterator<Cell> celle = riga.cellIterator();
				while (celle.hasNext()) {
					Cell cella = celle.next();
					switch (cella.getColumnIndex()) {
					case COGNOME_INDEX:
						arbitro.setCognome(cella.getStringCellValue());
						break;
					case NOME_INDEX:
						arbitro.setNome(cella.getStringCellValue());
						break;
					case MAX_SERIE_INDEX:
						arbitro.setMaxSerie(cella.getStringCellValue());
						break;
					case DISP_SETTIMANALE_INDEX:
						arbitro.setDispSettimanale(cella.getStringCellValue());
						break;
					case SQUADRE_DA_EVITARE_INDEX:
						String squadreDaEvitareCell = cella.getStringCellValue();
						if (squadreDaEvitareCell != null && !squadreDaEvitareCell.trim().isEmpty()) {
							String[] squadreDaEvitare = squadreDaEvitareCell.split(",");
							arbitro.setSquadreDaEvitare(new ArrayList<>());
							for (String squadra : squadreDaEvitare) {
								arbitro.getSquadreDaEvitare().add(squadra.trim());
							}
						}
						break;
					default:
						break;
					}
				}
				arbitri.add(arbitro);
			}
			
			workbook.close();
		} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
			System.err.println("Errore nell'apertura dell'excel");
			e.printStackTrace();
		}
		
		return arbitri;
	}
	
	private static void creaArbitraggi(List<Arbitro> arbitri) {
		try {
			FileInputStream file = new FileInputStream(new File(CALENDARIO_FILE_NAME));
			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);

			// rimuovo attuali validazioni
			if (sheet.getDataValidations() != null && sheet.getDataValidations().size() > 0) {
				((XSSFSheet) sheet).getCTWorksheet().unsetDataValidations();
				((XSSFSheet) sheet).getCTWorksheet().setDataValidations(null);
			}
			
			Iterator<Row> righe = sheet.rowIterator();
			while (righe.hasNext()) {
				Row riga = righe.next();
				// salto la prima riga (intestazione)
				if (riga.getRowNum() == 0) {
					continue;
				}
				
				Partita partita = new Partita();
				Iterator<Cell> celle = riga.cellIterator();
				while (celle.hasNext()) {
					Cell cella = celle.next();
					switch (cella.getColumnIndex()) {
					case DATA_INDEX:
						partita.setData(cella.getDateCellValue());
						break;
					case SERIE_INDEX:
						partita.setSerie(cella.getStringCellValue());
						break;
					case SQUADRA_A_INDEX:
						partita.setSquadraA(cella.getStringCellValue());
						break;
					case SQUADRA_B_INDEX:
						partita.setSquadraB(cella.getStringCellValue());
						break;
					default:
						break;
					}
				}
				
				List<Arbitro> primiArbitri = selezionaArbitri(arbitri, partita, true);
				List<Arbitro> secondiArbitri = selezionaArbitri(arbitri, partita, false);
				DataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
				DataValidationConstraint dvConstraint;
				CellRangeAddressList addressList;
				DataValidation dv;
				
				if (primiArbitri != null && primiArbitri.size() > 0) {
					String[] primiArbitriDataValidation = getArbitriDataValidation(primiArbitri);
					dvConstraint = dvHelper.createExplicitListConstraint(primiArbitriDataValidation);
					Cell cellaPrimo = riga.getCell(PRIMO_ARBITRO_INDEX);
					if (cellaPrimo == null) {
						cellaPrimo = riga.createCell(PRIMO_ARBITRO_INDEX);
					}
					cancellaCellaSeArbitroNonDisponibile(cellaPrimo, primiArbitriDataValidation);
					addressList = new CellRangeAddressList(cellaPrimo.getRowIndex(), cellaPrimo.getRowIndex(), cellaPrimo.getColumnIndex(), cellaPrimo.getColumnIndex());
					dv = dvHelper.createValidation(dvConstraint, addressList);
					dv.setShowErrorBox(true);
					sheet.addValidationData(dv);
				}
				
				if (secondiArbitri != null && secondiArbitri.size() > 0) {
					String[] secondiArbitriDataValidation = getArbitriDataValidation(secondiArbitri);
					dvConstraint = dvHelper.createExplicitListConstraint(secondiArbitriDataValidation);
					Cell cellaSecondo = riga.getCell(SECONDO_ARBITRO_INDEX);
					if (cellaSecondo == null) {
						cellaSecondo = riga.createCell(SECONDO_ARBITRO_INDEX);
					}
					cancellaCellaSeArbitroNonDisponibile(cellaSecondo, secondiArbitriDataValidation);
					Cell cellaRefertista = riga.getCell(REFERTISTA_INDEX);
					if (cellaRefertista == null) {
						cellaRefertista = riga.createCell(REFERTISTA_INDEX);
					}
					cancellaCellaSeArbitroNonDisponibile(cellaRefertista, secondiArbitriDataValidation);
					addressList = new CellRangeAddressList(cellaSecondo.getRowIndex(), cellaRefertista.getRowIndex(), cellaSecondo.getColumnIndex(), cellaRefertista.getColumnIndex());
					dv = dvHelper.createValidation(dvConstraint, addressList);
					dv.setShowErrorBox(true);
					sheet.addValidationData(dv);
				}
			}
			
			file.close();
			
			FileOutputStream outputStream = new FileOutputStream(CALENDARIO_FILE_NAME);
			workbook.write(outputStream);
			
			workbook.close();
			outputStream.close();
		} catch (IOException | EncryptedDocumentException e) {
			System.err.println("Errore nell'apertura dell'excel");
			e.printStackTrace();
		}
	}

	private static List<Arbitro> selezionaArbitri(List<Arbitro> arbitri, Partita partita, boolean isPrimoArbitro) {
		List<Arbitro> arbitriPossibili = new ArrayList<>();
		
		for (Arbitro arbitro : arbitri) {
			// se una squadra nella partita in esame è da evitare per quell'arbitro, non può arbitrare
			if (arbitro.getSquadreDaEvitare() != null) {
				boolean isSquadraCoincidente = false;
				for (String squadraDaEvitare : arbitro.getSquadreDaEvitare()) {
					if (isSquadraCoincidente(squadraDaEvitare, partita.getSquadraA(), partita.getSquadraB())) {
						isSquadraCoincidente = true;
						break;
					}
				}
				if (isSquadraCoincidente) {
					continue;
				}
			}
			// se sto selezionando un primo arbitro, controllo che possa arbitrare nella serie della partite in esame
			if (isPrimoArbitro) {
				if (arbitro.getMaxSerie() == null || arbitro.getMaxSerie().isEmpty()) {
					// non è un primo arbitro
					continue;
				}
				if (!isAbilitatoSerie(arbitro.getMaxSerie(), partita.getSerie())) {
					continue;
				}
			}
			// se nel giorno della settimana della partita in esame l'arbitro non ha dato disponibilità, non può arbitrare
			if (!isGiornoDisponibile(arbitro.getDispSettimanale(), partita.getData())) {
				continue;
			}
			
			// ha superato tutti i controlli --> può arbitrare
			arbitriPossibili.add(arbitro);
		}
		
		return arbitriPossibili;
	}
	
	private static String[] getArbitriDataValidation(List<Arbitro> arbitri) {
		String[] listaArbitri = new String[arbitri.size()];
		for (int i = 0; i < arbitri.size(); i++) {
			Arbitro arbitro = arbitri.get(i);
			listaArbitri[i] = arbitro.getCognome() + " " + arbitro.getNome();
		}
		
		return listaArbitri;
	}

	private static boolean isSquadraCoincidente(String squadra, String squadraA, String squadraB) {
		return squadra != null && !squadra.isEmpty()
				&& (squadra.trim().equalsIgnoreCase(squadraA.trim())
						|| squadra.trim().equalsIgnoreCase(squadraB.trim()));
	}

	private static boolean isAbilitatoSerie(String maxSerie, String serie) {
		switch (maxSerie) {
		case "A1":
			return true;
		case "A2":
			return !"A1".equals(serie);
		case "B1":
			return !"A1".equals(serie) && !"A2".equals(serie);
		case "B2":
			return !"A1".equals(serie) && !"A2".equals(serie) && !"B1".equals(serie);
		case "C1":
			return "C1".equals(serie) || "C2".equals(serie);
		case "C2":
			return "C2".equals(serie);
		default:
			break;
		}
		return false;
	}

	private static boolean isGiornoDisponibile(String dispSettimanale, Date data) {
		Calendar c = Calendar.getInstance();
		c.setTime(data);
		int giornoSettimana = c.get(Calendar.DAY_OF_WEEK);
		switch (giornoSettimana) {
		case Calendar.MONDAY:
			return '1' == dispSettimanale.charAt(0);
		case Calendar.TUESDAY:
			return '1' == dispSettimanale.charAt(1);
		case Calendar.WEDNESDAY:
			return '1' == dispSettimanale.charAt(2);
		case Calendar.THURSDAY:
			return '1' == dispSettimanale.charAt(3);
		case Calendar.FRIDAY:
			return '1' == dispSettimanale.charAt(4);
		case Calendar.SATURDAY:
			return '1' == dispSettimanale.charAt(5);
		case Calendar.SUNDAY:
			return '1' == dispSettimanale.charAt(6);
		default:
			break;
		}
		return false;
	}

	private static void cancellaCellaSeArbitroNonDisponibile(Cell cella, String[] primiArbitriDataValidation) {
		if (cella.getStringCellValue() != null) {
			boolean trovato = false;
			for (String arbitro : primiArbitriDataValidation) {
				if (arbitro.equals(cella.getStringCellValue())) {
					trovato = true;
					break;
				}
			}
			if (!trovato) {
				// l'arbitro precedentemente inserito nella cella non è più disponibile
				cella.setCellValue("");
			}
		}
	}
	
}
