/*
 * Cognome: Amadori
 * Nome: Maurizio
 * Matricola: 0001078717
 * E-mail: maurizio.amadori4@studio.unibo.it
 *
 * Esercizio3.java - risolve il problema dello zaino, nella formulazione
 * seguente: dato un insieme di n oggetti x_0, x_1, ... x_{n-1} tali
 * che l'oggetto x_i abbia peso p[i], determinare un sottoinsieme di oggetti
 * di peso minore o uguale a P il cui peso sia massimo possibile. Reitera
 * l'algoritmo finchè non si finiscono gli oggetti, tenendo conto di quanti
 * zaini sono necessari per memorizzare tutti gli oggetti.
 *
 * Tutti i pesi devono essere interi positivi  0 < x <= P.
 *
 * • Come sono definiti i sottoproblemi:
 *      dato uno "zaino" di capienza P e n oggetti caratterizzati da un peso p[i]
 *      definiamo V[i][j] come il massimo peso che può essere ottenuto dai primi i<=n
 *      oggetti contenuti in uno zaino di capacità j<=P
 *
 * • Come sono definite le soluzioni a tali sottoproblemi:
 *      Y insieme contente gli n oggetti iniziali
 *      vogliamo definire un sottoinsieme di oggetti X ∈ Y tali che
 *          ∑p[x] ≤ j con x∈Y tale che ∑p[x] sia massima
 *
 * • Come si calcolano le soluzioni nei casi base:
 *      zaino di capienza 0
 *          V[i, 0] = 0 per ogni i = 1..n
 *      ho a disposizione solo l'oggetto 1
 *          V[1, j] = p[1] se j ≥ p[1]
 *          V[1, j] = 0 se j < p[1]
 *
 * • Come si calcolano le soluzioni nel caso generale:
 *      V[i, j] = V[i - 1, j] se j < p[i]
 *      V[i, j] = max{ V[i - 1, j], V[i - 1, j - p[i] ] + p[i] } se j ≥ p[i]
 *      le condizioni verificano che l'oggetto ci stia e poi si va a selezionare la soluzione migliore
 *
 * • Calcolo del costo asintotico dell'algoritmo implementato:
 *      il metodo risolvi ha un costo pari a n(n*P+n*P) dove
 *      n è il numero di file e P la dimensione dello zaino
 *      quindi il costo computazionale risulta essere nell'ordine
 *      di O(P*n)
 */
// per compilare usare: javac Esercizio3.java
// per eseguire usare: java -cp . Esercizio3 input.txt

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Esercizio3{

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Errore usa: java -cp . Esercizio3 input.txt");
            System.exit(1);
        }

        final int capacitaCD = 650; //capacità di ogni unità di memorizzazione
        int nFile =-1; //numero di oggetti da memorizzare
        Node[] file = null;

        Locale.setDefault(Locale.US);
        try {
            File inputFile = new File(args[0]);
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            if (br.ready()) {
                nFile = Integer.parseInt(br.readLine());
            } else {
                System.out.println("Il file non contiene le informazioni necessarie");
                System.exit(1);
            }

            file = new Node[nFile];

            int i = 0;
            while (br.ready()) {
                String line = br.readLine();
                String[] dati = line.split(" ");
                file[i] = new Node(dati[0], Integer.parseInt(dati[1]));
                i++;
            }
        }catch (FileNotFoundException e){
            System.err.println("Errore durante l'apertura del file: ");
        }catch (IOException e){
            System.err.println("Errore durante lo svolgimento di IO sul file: ");
        }

        if(file!=null) {
            //Definisco il problema e ne determino una soluzione
            ZainoSoloPesiPD z = new ZainoSoloPesiPD(file, capacitaCD);
            z.risolvi();
        }
    }

    /**
     * Classe adatta a risolvere il problema dello zaino nella versione
     * solo con i pesi e senza "l'utilità"
     */
    public static class ZainoSoloPesiPD {
        int numeroDiCD = 0;
        List<Node> files;
        Node[] p; // p[i] e' il peso dell'oggetto i-esimo
        int P; // capacita' massima dello zaino.

        /**
         * Istanzia un nuovo oggetto di questa classe, prendendo come parametro:
         *
         * @param files elenco di tutti i file di input
         * @param capacitaCD capacita del CD
         */
        public ZainoSoloPesiPD(Node[] files, int capacitaCD) {
            this.files=new ArrayList<>();
            this.p=new Node[capacitaCD];
            int j = 0;
            for (Node i : files) {
                this.files.add(new Node(i.getNomeFile(), i.getPeso()));
                this.p[j] = new Node(i.getNomeFile(), i.getPeso());
                j++;
            }

            this.P = capacitaCD;
        }

        /**
         * Stampa la soluzione ottima per ogni singolo sottoproblema di massimizzazione dello spazio utilizzato.
         * Questo metodo dovrebbe essere invocato solo dopo l'esecuzione del metodo risolvi().
         * @param p array di file contenete i dati del problema
         * @param use matrice contenente nella posizione: use[i][j] = true se l'oggetto x_i fa 
         *            parte della soluzione ottima che ha valore V[i][j]
         * @param V matrice contenente nella posizione: V[i][j] e' il peso massimo che si puo' ottenere
         *          inserendo un sottoinsieme degli oggetti x_0, x_1,... x_i in un disco di capacita' massima j.
         */
        private void stampaSoluzione(Node[] p, boolean[][] use, int[][] V) {
            int j = P;
            int i = p.length - 1;
            stampaSoluzione(p, use, i, j);
            System.out.println("Spazio libero: "+ (P-V[p.length - 1][P]));
        }

        /**
         * metodo ricorsivo che permette la stampa in output dei dati nell'ordine desiderato
         * dal primo all'ultimo preso in considerazione durante la fase di input da file
         * @param use matrice contenente nella posizione: use[i][j] = true se l'oggetto x_i fa 
         *            parte della soluzione ottima che ha valore V[i][j]
         * @param p array di file contenete i dati del problema
         * @param i indice del file che sto controllando come è stato valutato
         * @param j indice della dimensione del sottoproblema che sto prendendo in considerazione
         */
        private void stampaSoluzione(Node[] p, boolean[][] use, int i ,int j){
            while (i >= 0) {
                if (use[i][j]) {
                    //rimuovo l'oggetto dalla lista in modo da poter continuare con il riempimento dei cd
                    this.files.remove(p[i]);
                    stampaSoluzione(p, use, i-1, j-p[i].getPeso());
                    System.out.println(p[i].getNomeFile()+" "+p[i].getPeso());
                    break;
                }
                i--;
            }
        }

        /**
         * Risolve una versione del problema dello zaino senza valori di utilità,
         * la massimizzazione veine fatta sui pesi (in modo da cercare di riempire
         * lo zaino) utilizzando la programmazione dinamica. Ritorna il valore
         * massimo dei file contenuti nel disco. Al termine dell'ennesimo ciclo
         * while le matrici V[][] e use[][] contengono i valori calcolati durante
         * l'esecuzione del ciclo per riempire l'ennesimo CD.
         */
        public void risolvi() {
            while (!files.isEmpty()) {
                int i=0, j;
                int n = this.files.size(); // numero di oggetti

                int[][] V; // V[i][j] e' il peso massimo che si puo' ottenere
                // inserendo un sottoinsieme degli oggetti x_0, x_1,
                // ... x_i in uno zaino di capacita' massima j.

                boolean[][] use; // use[i][j] = true se l'oggetto x_i fa parte della
                // soluzione ottima che ha valore V[i][j]

                Node[] p = new Node[files.size()];

                for (Node dati : files) {
                    p[i] = new Node(dati.getNomeFile(), dati.getPeso());
                    i++;
                }


                numeroDiCD++;
                V = new int[n][1 + P];
                use = new boolean[n][1 + P];
                // Inizializza prima riga di V e use. In questa implementazione
                // non inizializziamo esplicitamente la prima colonna di V e
                // use, in quanto il calcolo di V[i][0] ricade nel caso "j < p[i]"
                // e viene correttamente settato a V[i-1][0].
                for (j = 0; j <= P; j++) {
                    if (j < p[0].getPeso()) {
                        V[0][j] = 0;
                        use[0][j] = false;
                    } else {
                        V[0][j] =  p[0].getPeso();
                        use[0][j] = true;
                    }
                }

                // Calcola gli altri elementi delle matrici V[i][j] e use[i][j]
                // per i=1..n-1, j=1..P
                for (i = 1; i < n; i++) {
                    for (j = 0; j <= P; j++) {
                        if ( j >= p[i].getPeso() && V[i - 1][j - p[i].getPeso()] + p[i].getPeso() > V[i - 1][j]) {
                            V[i][j] = V[i - 1][j - p[i].getPeso()] + p[i].getPeso();
                            use[i][j] = true;
                        } else {
                            V[i][j] = V[i-1][j];
                            use[i][j] = false;
                        }
                    }
                }

                System.out.println("\nDisco: "+numeroDiCD);
                stampaSoluzione(p,use,V);
            }
        }
    }

    /**
     * classe che permette di memorizzare gli oggetti ottenuti in input
     * attraverso i campi:
     *  - nomeFile: stringa contenente il nome del file da memorizzare
     *  - peso: intero rappresentante il peso in memoria in MB del file
     */
    public static class Node{
        String nomeFile;
        int peso;

        public Node(String nomeFile, int peso) {
            this.nomeFile = nomeFile;
            this.peso = peso;
        }

        public String getNomeFile() {
            return nomeFile;
        }

        public int getPeso() {
            return peso;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Node){
                Node n = (Node) obj;
                return n.nomeFile.equals(this.nomeFile) && n.peso == this.peso;
            }else{
                //l'oggetto passato come parametro non appartiene alla classe corretta
                throw new IllegalArgumentException();
            }
        }
    }
}
