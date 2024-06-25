/*
 * Cognome: Amadori
 * Nome: Maurizio
 * Matricola: 0001078717
 * E-mail: maurizio.amadori4@studio.unibo.it
 *
 * Il programma partendo da una cella di una scacchiera di dimensione indicata
 * in input esegue un algoritmo di visita BFS applicando la mossa del cavallo
 * con l'obbiettivo di verificare se un cavallo possa raggiungere tutte le altre
 * celle libere della scacchiera.
 *
 * '.' -> cella libera
 * 'C' -> cella di partenza del cavallo e cella che il cavallo può raggiungere
 * 'X' -> cella occupata da un altra pedina
 */
// per compilare usare: javac Esercizio2.java
// per eseguire usare: java -cp . Esercizio2 input.txt

import java.io.*;
import java.util.*;
public class Esercizio2 {
    static final char punto = '.';
    static final char c = 'C';
    static final char x = 'X';
    static int n=0; //numero di righe scacchiera
    static int m=0; //numero di colonne scacchiera

    public static void main(String[] args) {
        Cella root = null;

        if (args.length != 1) {
            System.err.println("Errore usa: java -cp . Esercizio2 input.txt");
            System.exit(1);
        }

        try{
            File file = new File(args[0]);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            if(br.ready()){
                n=Integer.parseInt(br.readLine());
            }else{
                System.out.println("il file non contiene le informazioni necessarie");
                System.exit(1);
            }
            if(br.ready()){
                m=Integer.parseInt(br.readLine());
            }else{
                System.out.println("il file non contiene le informazioni necessarie");
                System.exit(1);
            }

            Cella[][] chest = new Cella[n][m];

            int i=0;
            //carico i valori nella scacchiera
            while (br.ready()) {
                String entry = br.readLine();
                char[] caratteri = entry.toCharArray();
                for(int j=0; j<m; j++){
                    if(caratteri[j]==c){
                        root = new Cella(c,i,j);
                        chest[i][j]=root;
                    }else if(caratteri[j]==punto){
                        chest[i][j]=new Cella(punto,i,j);
                    }else {
                        chest[i][j]=new Cella(x,i,j);
                    }
                }
                i++;
            }
            br.close();
            fr.close();

            /**
             * il cavallo può fare solo le seguenti mosse:
             * - 2 caselle verticali e 1 orizzontale
             * - 2 caselle orizzontali e 1 verticale
             * - 1 casella orizzontale e 2 verticali
             * - 1 casella verticale e 2 orizzontali
             * Quindi applico un algoritmo di visita BFS sul grafo sotto forma di scacchiera partendo
             * dalla posizione del cavllo. Con questa idea verifico se il grafo è completamente connesso,
             * se lo è allora ritorno true se no false.
             *
             * L'algoritmo proposto con tempo O(n+m) ottiene la soluzione nel caso pessimo dove:
             * n è il numero di righe della scacchiera e m è il numero di colonne
             **/
            Cella[][] outputChest = BFS.visit(root, chest);
            stampaScacchiera(outputChest);

        }catch (FileNotFoundException e){
            System.err.println("Errore il file '"+ args[0]+"' non e' stato trovato");
        }catch (IOException e){
            System.err.println("Errore durante lo svolgimento di IO sul file: '"+ args[0]);
        }
    }

    /**
     * Classe rappresentante la cella di una scacchiera e il suo contenuto
     */
    public static class Cella {
        //coordinate
        int x;
        int y;
        char carattere;

        public Cella(int x, int y){
            this.x=x;
            this.y=y;
        }

        public Cella(char c, int x, int y){
            this.carattere=c;
            this.x=x;
            this.y=y;
        }

        public char getCarattere() {
            return carattere;
        }

        public void setCarattere(char carattere) {
            this.carattere = carattere;
        }

        /**
         * crea un nuovo nodo che rappresenta la cella in cui
         * la pedina si è spostata sulla scacchiera rispetto
         * alla posizione della cella attuale
         *
         * @param valX valore rappresentante lo spostamento orizzontale
         * @param valY valore rappresentante lo spostamento verticale
         * @return Node, risultante dallo spostamento
         */
        public Cella increaseXY(int valX, int valY){
            if(valX+x>=0&&valX+x<n && valY+y>=0&&valY+y<m){
                return new Cella(this.x+valX,this.y+valY);
            }else {
                return null;
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public String toString(){
            return "("+x+","+y+")";
        }
    }

    public static class BFS{
        /**
         * Effettua una visita BFS sul grafo preso come parametro a partire
         * dalla root e applicando a ogni iterazione la mossa del cavallo
         *
         * @param root cella di partenza del BFS
         * @param grafo matrice rappresentante la scacchiera
         * @return Cella[][] contenente la matrice dopo la visita in cui
         * tutte le posizioni raggiungibili dal cavallo contengono 'C', quelle
         * non raggiungibili contengono '.' e quelle occupate contengono 'X'
         */
        public static Cella[][] visit(Cella root, Cella[][] grafo) {
            boolean[][] visited =new boolean[grafo.length][grafo[0].length];

            Queue<Cella> q = new LinkedList<Cella>(); // A FIFO queue
            q.add(root);

            while ( !q.isEmpty() ) {
                final Cella u = q.poll();
                for (Cella i : nodiVicini(u, grafo)) {
                    if (!visited[i.getX()][i.getY()]) {
                        q.add(i);
                        grafo[i.getX()][i.getY()].setCarattere(c);
                    }
                }
                visited[u.getX()][u.getY()]=true;
            }

            return grafo;
        }

        /**
         * Calcola tutte le possibili mosse che il cavallo può fare partendo da
         * root e restituisce una lista di celle in cui il cavallo può andare a
         * posizionarsi
         *
         * @param root cella di partenza
         * @param grafo matrice rappresentante la scacchiera
         * @return LinkedList<Cella> lista di tutte le celle raggiungibili da root per il cavallo
         */
        private static LinkedList<Cella> nodiVicini(Cella root, Cella[][] grafo) {
            LinkedList<Cella> list = new LinkedList<Cella>();

            //array contenente tutti i possibili spostamenti del cavallo dalla posizione corrente
            Cella[] possibiliVicini = new Cella[]{root.increaseXY(2,1),
                                                root.increaseXY(2,-1),
                                                root.increaseXY(-2,1),
                                                root.increaseXY(-2,-1),
                                                root.increaseXY(1,2),
                                                root.increaseXY(-1,-2),
                                                root.increaseXY(-1,2),
                                                root.increaseXY(1,-2)};

            for(Cella n : possibiliVicini){
                if(n!=null) {
                    if (grafo[n.getX()][n.getY()].getCarattere() == punto && n.carattere != c) {
                        list.add(n);
                    }
                }
            }

            return list;
        }
    }

    /**
     * Stampa la scacchiera presa come parametro e un flag che è a true
     * se tutte le celle libere sono raggiungibili dal cavallo e false altrimenti
     *
     * @param chest scacchiera sotto forma di matrice
     */
    public static void stampaScacchiera(Cella[][] chest) {
        Boolean raggiungibilitaCompleta = true;

        for(int i=0; i< chest.length; i++){
            for(int j=0; j< chest[0].length; j++){
                if (chest[i][j].getCarattere()==punto) {
                    raggiungibilitaCompleta = false;
                }
                System.out.print(chest[i][j].getCarattere());
            }
            System.out.println();
        }

        System.out.println(raggiungibilitaCompleta);
    }
}