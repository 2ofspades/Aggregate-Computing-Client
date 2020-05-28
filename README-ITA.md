# Aggregate Computing Client

Il goal del progetto è quello di sviluppare una applicazione di messaggistica d'emergenza, utilizzando la programmazione aggregata e la libreria progettata in [Aggregate Computing Backend](https://github.com/matteozattoni/Aggregate-Computing-Backend). Si vuole permettere ai vari Client di potersi individuare e scambiarsi informazioni utilizzando questa libreria insieme ad alcuni framework di network (come Wifi Aware e BLE) in cui è naturale pensare a dispositivi direttamente connessi come vicini.
L’utilizzo di questa applicazione è studiata per tutti quei scenari in cui la rete Internet venga a mancare, come nel caso di disastri naturali (calamità) e/o disastri artificiali (guerre) in cui l’infrastruttura delle telecomunicazioni potrebbe risultare in parte o del tutto assente. La natura dei messaggi potrebbe essere varia, non solo testuale ma anche scambio di informazioni sulla propria posizione e relative distanze, oppure con l’utilizzo sempre maggiore degli Smart Watch anche informazioni di natura medica come ad esempio parametri vitali (in caso di emergenza).

In questa prima fase si pone l'obbiettivo di scrivere il programma protelis ed implementare quelle parti dell' Architettura fondamentali per lo scambio di messaggi e la loro persistenza nel tempo, insieme ad una UI elementare dal punto di vista grafico ma che risponda ai cambiamenti del modello. Per ora viene utilizzato come framework network quello definito di default nella libreria backend ovvero un indirizzo locale, in una seconda fase invece si andranno ad definire dei framework più complessi come [questo](https://developer.android.com/guide/topics/connectivity/wifi-aware). Essendo la parte di framework network estendibile e indipendente dal resto dell'Architettura non sarà un problema inserirla nelle fasi successive del progetto.

## **Requisiti**

*  ###### Funzionalità:
  * Il Client esegue (o cerca, *vedere modalità remota*) un ServerDevice al quale connettersi
  * Il Client esegue il programma protelis (o aspetta solo il risultato, *vedere modalità ibrida*), scambiando messaggi con il Server


* ###### Modalità remota
  - Il Client (tramite la libreria in Aggregate Computing Backend) cerca un server a cui connettersi attraverso i framework che il Client device di utilizzare


* ###### Modalità ibrida:
  - Il Client viene considerato un device Lightweight (tutte le informazioni nella parte Backend) riceve solo il risultato del programma protelis da parte del server
  - Possibilità che il Client possa inviare al server un programma protelis arbitrario da eseguire (modalità ancora da aggiungere)


* ###### Il programma protelis deve essenzialmente offrire i seguenti risultati al Client:
  - sapere quali sono i device raggiungibili
  - raccogliere informazioni sui device presenti
  - poter inviare messaggi ad un Device arbitrario (best-effort, altri servizi devono essere implementati a livello applicativo)
  - sapere la distanza tra i device presenti (la metrica utilizzata sono gli Hop, in futuro potrà cambiare)

## **Architettura**

La parte client si divide essenzialmente in 4 parti

* il package **controller/protelis** sono presenti le classi che si interfacciano direttamente al programma protelis, insieme alla classi che definiscono le informazioni (la factory utilizzata per istanziare le classi come UserUID per identificare i device)
* il package **controller/data** qui sono presenti le classi che hanno il compito di rendere persistenti (attraverso Room) i dati del livello applicativo, come i profili ed i messaggi
* il package **view/** qui sono presenti le classi che visualizzano i dati come la lista degli user ed messaggi associati ad essi (utilizzando LiveData con Adapter e ViewHolder)
* il programma protelis (**res/raw/awarenet.pt**), la parte più importante del progetto. Compie essenzialmente queste operazioni
  - uno stato condiviso che rappresenta l'insieme dei Device online
  - uno stato condiviso che rappresenta l'insieme dei profili dei Device
  - una lista dove ogni elemento rappresenta una coppia ID e la distanza a questo ID
  - un stato condiviso che rappresenta l'insieme dei messaggi da inviare, ogni elemento rappresenta un messaggio e la distanza (propria) a quella destinazione, ad ogni iterazione vengono presi in esame gli stati dei vicini, se è presente un messaggio già presente nell'insieme allora vengono confrontate le distanze, se il vicino ha la distanza minore il messaggio viene eliminato dall'insieme, per ogni messaggio che non è presente nell'insieme, se la distanza è minore rispetto a quella del vicino allora il messaggio viene aggiunto all'insieme.
