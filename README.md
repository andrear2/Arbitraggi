# Arbitraggi
Programma per gestire gli arbitraggi di hitball

Il programma legge i seguenti file excel.

L'excel degli arbitri contiene le informazioni:
- Cognome
- Nome
- Massima serie che può arbitrare (solo se primo arbitro)
- Disponibilità settimanale (per ogni giorno della settimana, dal lunedì alla domenica, 0 indica che non può arbitrare, 1 che può)
- Squadre da evitare (contiene tutte le squadre che l'arbitro non può arbitrare, separate da virgola)

L'excel del calendario contiene le informazioni:
- Data
- Serie
- Squadra A
- Squadra B

Una volta lanciato, compila una lista dei possibili 1° Arbitro, 2° Arbitro e Refertista per ogni riga dell'excel del calendario.
