/*
 * Cognome: Amadori
 * Nome: Maurizio
 * Matricola: 0001078717
 * E-mail: maurizio.amadori4@studio.unibo.it
 *
 * Il programma carica tutti i dati relativi alle occorrenze delle parole in un libro
 * in una Hash Table e su richiesta ritorna il numero di occorrenze della parola 'x'
 *
 * L'Hash Table è implementata con code di trabocco e si ridimensiona automaticamente
 * sulla base di una soglia di riempimento (loadThreshold)
 */
// per compilare usare: javac Esercizio1.java
// per eseguire usare: java -cp . Esercizio1 occorrenze.txt parole.txt

import java.io.*;
public class Esercizio1 {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Errore usa: java -cp . Esercizio1 input1.txt input2.txt");
            System.exit(1);
        }

        HashTable ht = new HashTable();

        try {
            File file = new File(args[0]);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            //carico i valori nella struttura dati
            while (br.ready()) {
                String entry = br.readLine();
                String[] deta1 = entry.split(",");
                if(deta1.length==2) {
                    ht.aggiungiOccorrenze(deta1[1], Integer.parseInt(deta1[0]));
                }else {
                    throw new IOException();
                }
            }
            br.close();
            fr.close();
        }catch(NumberFormatException e){
            System.err.println("Errore il file: '"+ args[0] +"' non contiene i dati nel formato corretto");
            System.exit(1);
        }catch (FileNotFoundException e){
            System.err.println("Errore il file: '"+ args[0] +"' non e' stato trovato");
            System.exit(1);
        }catch (IOException e){
            System.err.println("Errore durante la lettura dei file: '"+ args[0] +"'");
            System.exit(1);
        }

        try{

            File file1 = new File(args[1]);
            FileReader fr1 = new FileReader(file1);
            BufferedReader br1 = new BufferedReader(fr1);

            //output
            while (br1.ready()) {
                String entry = br1.readLine();
                entry = entry.toLowerCase();
                System.out.println(entry+", "+ht.occorrenzeParola(entry));
            }

            br1.close();
            fr1.close();
        }catch (FileNotFoundException e){
            System.err.println("Errore il file: '"+ args[1] +"' non e' stato trovato");
            System.exit(1);
        }catch (IOException e){
            System.err.println("Errore durante la lettura dei file: '"+ args[1] +"'");
            System.exit(1);
        }
    }

    /**
     * Classe rappresentante una Hash Table con code di trabocco
     */
    public static class HashTable{
        final int k = 128;//initial size
        Node[] bucket;
        final double loadThreshold = 0.75;
        int size = 0;

        public HashTable(){
            bucket = new Node[k];
        }

        public HashTable(int size){
            bucket = new Node[size];
        }

        private int hashFunction(String key){
            return Math.abs(key.hashCode()) % bucket.length;
        }

        /**
         * aumenta le dimensioni del bucket in base a quanto è
         * pieno rispetto alla soglia impostata (loadThreshold)
         */
        private void increaseSize(){
            this.size++;
            if((double)this.size/this.bucket.length>this.loadThreshold){
                HashTable newHashTable = new HashTable(this.bucket.length*2);
                for(Node node : this.bucket){
                    while (node!=null){
                        newHashTable.aggiungiOccorrenze(node.getWord(),node.getOccorrenze());
                        node = node.getNext();
                    }
                }
                this.bucket = newHashTable.bucket;
            }
        }

        /**
         * aggiunge una nuova parola alla Hash Table se parola non è presente
         * o incrementa l'occorrenza di n_occorrenze se parola è già presente.
         * Il metodo converte autonomamente tutte le parole in minuscolo
         *
         * @param parola stringa da aggiungere all'Hash Table
         * @param n_occorrenze numero di occorrenze in cui la stringa appare
         */
        private void aggiungiOccorrenze(String parola, int n_occorrenze){
            parola = parola.toLowerCase();
            int i = hashFunction(parola);
            if(this.bucket[i] == null){
                this.bucket[i] = new Node(parola, n_occorrenze);
                this.increaseSize();
            }else{
                Node currentNode = this.bucket[i];
                Node found = find(currentNode, parola);

                if(found!=null){
                    found.increaseOccorrenze(n_occorrenze);
                }else{
                    Node newNode = new Node(parola, n_occorrenze);
                    newNode.setNext(this.bucket[i]);
                    this.bucket[i] = newNode;
                    this.increaseSize();
                }
            }
        }

        /**
         * Prende in input una stringa e la cerca all'interno della hash table.
         * Autonomamente la parola viene convertita e ricercata in minuscolo
         *
         * @param parola parola di cui si è interessati a sapere la ricorrenza
         * @return Il numero di occorrenze della "parola" o -1 se non viene trovata
         */
        public int occorrenzeParola(String parola){
            parola = parola.toLowerCase();
            int i = hashFunction(parola);
            if(this.bucket[i] == null){
                return 0;
            }else{
                Node currentNode = this.bucket[i];
                Node found = find(currentNode, parola);

                return found == null ? 0 : found.getOccorrenze();
            }
        }

        /**
         * Cerca la parola word nella lista di trabocco e se la trova ritorna
         * il nodo che la contine se no null.
         *
         * @param currentNode nodo da cui iniziare la ricerca
         * @param word parola da cercare
         * @return Node contenente la parola o null se non viene trovata
         */
        private Node find(Node currentNode, String word){
            Node found = null;
            //scorro la lista di trabocco finchè non trovo il nodo
            while (currentNode!=null && found==null) {
                if (currentNode.getWord().equals(word)) {
                    found = currentNode;
                }else {
                    currentNode = currentNode.getNext();
                }
            }
            return found;
        }
    }

    /**
     * classe rappresentante il nodo di una lista monodirezionale
     */
    public static class Node {
        String word;//key
        int occorrenze;
        Node next;

        public Node(String word, int occorrenze) {
            this.word = word;
            this.occorrenze = occorrenze;
        }

        public int getOccorrenze() {
            return occorrenze;
        }

        public String getWord() {
            return word;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public void increaseOccorrenze(int occorrenze) {
            this.occorrenze += occorrenze;
        }

        @Override
        public String toString() {
            return getWord();
        }
    }
}