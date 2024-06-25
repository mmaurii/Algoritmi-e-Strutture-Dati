/*
 * Cognome: Amadori
 * Nome: Maurizio
 * Matricola: 0001078717
 * E-mail: maurizio.amadori4@studio.unibo.it
 *
 * Il programma calcola tutti i percorsi minimi disgiunti partendo da ogni nodo del grafo
 * (sorgente) e arrivando ad ogni altro possibile nodo del grafo (destinazione). Il problema
 * viene risolto applicando dijkstra n(n-1) volte ovvero per ogni coppia di nodi e rimuovendo
 * iterativamente i percorsi minimi dal grafo per ogni coppia di nodi in modo da cercare se c'è
 * un altro percorso minimo con lo stesso costo del precedente.
 *
 * Costo computazionale in termini di nodi e di archi dell'algoritmo proposto è:
 *      O((n^2)(n+m)log(n)) in quanto l'algoritmo di dijkstra ha costo (n+m)log(n)
 *      e la sua iterazione viene fatta n^2 volte. Gli altri costi sono trascurabili.
 *      quini il costo computazionale risultatnte è O((n^3)log(n)) se m è nell'ordine di n.
 *      O((n^2)mlog(n)) altrimenti.
 *
 * Da questo si può dedurre che per risolvere il problema degli 'all shortest paths' di un grafo
 * sia più conveniente utilizzare l'algoritmo di Floyd-Warshall in quanto ha costo O(n^3)
 */
// per compilare usare: javac Esercizio4.java
// per eseguire usare: java -cp . Esercizio4 input.txt

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class Esercizio4 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Errore usa: java -cp Esercizio4 input.txt");
            System.exit(1);
        }

        Dijkstra sp = new Dijkstra(args[0]);

        final long start_t = System.currentTimeMillis();

        sp.pathMinDisgiunti();

        final long end_t = System.currentTimeMillis();
        final double elapsed = (end_t - start_t) / 1000.0;
        System.out.format("Elapsed time %.4f seconds\n", elapsed);
    }

    public static class Dijkstra {
        int n;      // number of nodes in the graph
        int m;      // number of edges in the graph
        Vector<LinkedList<Edge>> adjList; // adjacency list
        int source; // the source node
        MinPath[] minPaths;

        /**
         * Constructs a Dijkstra object; read input from inputf
         *
         * @param inputf the input file name
         */
        public Dijkstra(String inputf) {
            readGraph(inputf);
        }

        /**
         * Prints the shortest path from |source| to |dst|
         *
         * @param p percorso da stampare
         * @param dst destinatario da cui iniziare a leggere il percorso
         */
        protected void print_path(int[] p, int dst) {
            if (dst == source)
                System.out.print(dst);
            else if (p[dst] < 0)
                System.out.print("Irraggiungibile");
            else {
                print_path(p, p[dst]);
                System.out.print("->" + dst);
            }
        }

        /**
         * Prints the shortest path from |source| to |dst|
         *
         * @param p lista di tutti i percorsi minimi disgiunti per il
         *          destinatario (dst) specificato
         * @param dst destinatario da cui iniziare a leggere il percorso
         */
        protected void print_path(LinkedList<int[]> p, int dst) {
            for(int[] a : p) {
                System.out.printf("%4d %4d %12.4f ", source, dst, minPaths[dst].getWeight());
                print_path(a, dst);
                System.out.println();
            }
        }

        /**
         * Stampa tutti i percorsi minimi disgiunti da source a tutti gli altri nodi
         */
        protected void print_path() {
            //intestazione
            System.out.println("Source = " + source);
            System.out.println("   s    d         dist path");
            System.out.println("---- ---- ------------ -------------------");

            //percorsi
            for (int dst = 0; dst < n; dst++) {
                if(minPaths[dst]!=null) {
                    print_path(minPaths[dst].getPaths(), dst);
                }else{
                    System.out.printf("%4d %4d", source, dst);
                    System.out.println("\tIrraggiungibile");
                }
            }
            System.out.println();
        }

        /**
         * Attraverso la ripetizione dell'algoritmo di dijkstra trova TUTTI i percorsi
         * minimi disgiunti partendo da ogni nodo e arrivando a tutti gli altri
         */
        public void pathMinDisgiunti() {
            for (int src = 0; src < n; src++) {
                this.source=src;
                this.minPaths = new MinPath[n];

                for (int dst = 0; dst < n; dst++) {
                    Vector<LinkedList<Edge>> adj = new Vector<LinkedList<Edge>>();

                    for(int i = 0; i<n; i++){
                        LinkedList<Edge> listaDiAdiacenza = new LinkedList<>();
                        for(Edge e : adjList.get(i)) {
                            listaDiAdiacenza.add(new Edge(e));
                        }
                        adj.add(listaDiAdiacenza);
                    }

                    percorsiDisgiunti(src, dst, adj);
                }
                print_path();
                this.minPaths =null;
            }
        }

        /**
         * Read the input graph from file inputf.
         * @param inputf file name
         */
        private void readGraph(String inputf) {
            Locale.setDefault(Locale.US);

            try {
                Scanner f = new Scanner(new FileReader(inputf));
                n = f.nextInt();
                m = f.nextInt();

                adjList = new Vector<>();

                for (int i = 0; i < n; i++) {
                    adjList.add(new LinkedList<Edge>());
                }

                for (int i = 0; i < m; i++) {
                    final int src = f.nextInt();
                    final int dst = f.nextInt();
                    final double weight = f.nextDouble();
                    assert (weight >= 0.0);
                    adjList.get(src).add(new Edge(src, dst, weight));
                    adjList.get(dst).add(new Edge(dst, src, weight));
                }
            } catch (IOException ex) {
                System.err.println(ex);
                System.exit(1);
            }
        }

        /**
         * Execute Dijkstra's shortest paths algorithm from node src to dst
         * and return true if there is a shortest path that weight is the same
         * of the previous one, otherwise false.
         *
         * @param src source node
         * @param dst destination node
         * @param minPathWeight peso ottenuto dall'esecuzione precedente,
         *                      al primo round deve essere setteto a max
         * @param adj liste di adiacenza rappresentanti il grafo
         */
        private boolean dijkstra(int src, int dst, double minPathWeight, Vector<LinkedList<Edge>> adj) {
            MinHeap q = new MinHeap(n);
            double[] d = new double[n]; // array of distances from the source
            int[] p = new int[n];    // array of parents

            Arrays.fill(d, Double.POSITIVE_INFINITY);
            Arrays.fill(p, -1);
            d[src] = 0.0;
            for (int v = 0; v < n; v++) {
                q.insert(v, d[v]);
            }

            //assegno la distanza nel caso in cui destinatario e sorgente coincidano
            if(src==dst){
                p[src] = src;
                minPaths[dst] = new MinPath(new Edge(src, dst, d[dst]));
                minPaths[dst].addPath(p.clone());
                return false;
            }

            while (!q.isEmpty()) {
                final int u = q.min();
                q.deleteMin();

                if(u == dst){break;}

                for (Edge e : adj.get(u)) {
                    final int v = e.dst;

                    if (d[u] + e.w < d[v]) {
                        d[v] = d[u] + e.w;
                        q.changePrio(v, d[v]);
                        p[v] = u;
                    }
                }
            }

            //controllo che il nodo dst sia stato raggiunto
            if(p[dst]!=-1){
                //salvo il percorso
                int[] sp = new int[n];
                Arrays.fill(sp, -1);
                for (int i = dst; i != src; i=p[i]) {
                    sp[i] = p[i];
                }

                if (minPaths[dst]==null){
                    minPaths[dst] = new MinPath(new Edge(src, dst, d[dst]));
                }

                //controllo che il percorso minimo individuato sia conforme
                if(d[dst]==minPathWeight || minPathWeight==Double.POSITIVE_INFINITY) {
                    minPaths[dst].addPath(sp);
                    return true;
                }
            }
            return false;
        }

        /**
         * calcola tutti percorsi minimi disgiunti
         * partendo dal nodo src e arrivando a dst
         *
         * @param src nodo sorgente
         * @param dst nodo destinazione
         * @param adj liste di adiacenza rappresentanti il grafo
         */
        private void percorsiDisgiunti(int src, int dst, Vector<LinkedList<Edge>> adj) {
            //itero l'algoritmo di dijkstra finchè si trovano percorsi minimi con
            // peso uguale al primo percorso minimo trovato, andando a togliere a ogni
            // iterazione gli archi che compongono il percorso minimo
            double minPathWeight = Double.POSITIVE_INFINITY;
            while (dijkstra(src, dst, minPathWeight, adj)){
                minPathWeight= minPaths[dst].getWeight();
                rimuoviEdge(src, dst, adj);
            }
        }

        /**
         * rimuove gli archi componenti l'ultimo percorso minimo disgiunto trovato
         * dal grafo
         *
         * @param src nodo sorgente
         * @param dst nodo destinatario
         * @param adj liste di adiacenza rappresentanti il grafo
         */
        private void rimuoviEdge(int src, int dst, Vector<LinkedList<Edge>> adj) {
            //seleziono l'ultimo percorso aggiunto ed elimino l'arco
            //bidirezionale rappresentato da due archi diretti
            int [] path = minPaths[dst].getPaths().getLast();

            for (int i = dst; i != src; i=path[i]) {
                adj.get(i).remove(new Edge(i, path[i],0));
                adj.get(path[i]).remove(new Edge(path[i], i, 0));
            }
        }

    }


    /**
     * Edge of a weighted, directed graph
     */
    public static class Edge {
        final int src;
        final int dst;
        double w;

        /**
         * Build a directed edge (src, dst) with weight w
         */
        public Edge(int src, int dst, double w) {
            /* Dijkstra's algorithm requires that weights are
               non-negative */
            assert (w >= 0.0);

            this.src = src;
            this.dst = dst;
            this.w = w;
        }

        public Edge(Edge e){
            this.src = e.src;
            this.dst = e.dst;
            this.w = e.w;
        }

        @Override
        public boolean equals(Object obj) {
            //non considera che due archi possano essere connessi
            // da due archi distinti con pesi diversi
            if(obj instanceof Edge) {
                Edge e = (Edge) obj;
                return e.dst == dst && e.src == src;
            }else{
                //l'oggetto passato come parametro non appartiene alla classe corretta
                throw new IllegalArgumentException();
            }
        }

        public double getW() {
            return this.w;
        }
    }

    /**
     * Classe che descrive i percorsi per andare da un nodo sorgente a un
     * nodo destinatario in termini di nodi (numeri interi) da percorre
     * attraverso una lista di adiacenza mantenuta in un array di interi
     */
    public static class MinPath {
        Edge e;
        LinkedList<int[]> paths;

        public MinPath(Edge e){
            this.e = e;
            this.paths = new LinkedList<>();
        }

        public double getWeight() {
            return e.getW();
        }

        public LinkedList<int[]> getPaths() {
            return paths;
        }

        public void addPath(int[] p) {
            this.paths.add(p);
        }
    }

    /**
     * This class implements a min-heap of a given maximum initial size n.
     * The heap contains pairs (data, priority) where data is an integer
     * in the range 0..n-1, and priority is any real value.
     */
    public static class MinHeap {

        heapElem[] heap;
        /* pos[id] is the position of "id" inside the heap. Specifically,
           heap[pos[id]].key == id. This array is required to make
           decreaseKey() run in log(n) time. */
        int[] pos;
        int size, maxSize;

        /**
         * An heap element is a pair (id, priority), where
         * id is an integer in 0..(maxSize-1)
         */
        private static class heapElem {
            public final int data;
            public double prio;

            public heapElem(int data, double prio)
            {
                this.data = data;
                this.prio = prio;
            }
        }

        /**
         * Build an empty heap with at most maxSize elements
         */
        public MinHeap(int maxSize)
        {
            this.heap = new heapElem[maxSize];
            this.maxSize = maxSize;
            this.size = 0;
            this.pos = new int[maxSize];
            Arrays.fill(this.pos, -1);
        }


        /**
         * Return true iff index i is a valid index in the heap,
         * i.e., i>=0 and i<size
         */
        private boolean valid(int i)
        {
            return ((i >= 0) && (i < size));
        }

        /**
         * swap heap[i] with heap[j]
         */
        private void swap(int i, int j)
        {
            assert (pos[heap[i].data] == i);
            assert (pos[heap[j].data] == j);

            heapElem elemTmp = heap[i];
            heap[i] = heap[j];
            heap[j] = elemTmp;
            pos[heap[i].data] = i;
            pos[heap[j].data] = j;
        }

        /**
         * Return the index of the parent of heap[i]
         */
        private int parent(int i)
        {
            assert (valid(i));

            return (i+1)/2 - 1;
        }

        /**
         * Return the index of the left child of heap[i]
         */
        private int lchild(int i)
        {
            assert (valid(i));

            return (i+1)*2 - 1;
        }

        /**
         * Return the index of the right child of heap[i]
         */
        private int rchild(int i)
        {
            assert (valid(i));

            return lchild(i) + 1;
        }

        /**
         * Return true iff the heap is empty
         */
        public boolean isEmpty( )
        {
            return (size==0);
        }

        /**
         * Return true iff the heap is full, i.e., no more available slots
         * are available.
         */
        public boolean isFull( )
        {
            return (size > maxSize);
        }

        /**
         * Return the data of the element with lowest priority
         */
        public int min( )
        {
            assert ( !isEmpty() );
            return heap[0].data;
        }

        /**
         * Return the position of the child of i (if any) with minimum
         * priority. If i has no childs, return -1.
         */
        private int minChild(int i)
        {
            assert (valid(i));

            final int l = lchild(i);
            final int r = rchild(i);
            int result = -1;
            if (valid(l)) {
                result = l;
                if (valid(r) && (heap[r].prio < heap[l].prio)) {
                    result = r;
                }
            }
            return result;
        }

        /**
         * Exchange heap[i] with the parent element until it reaches the
         * correct position into the heap. This method requires time O(log n).
         */
        private void moveUp(int i)
        {
            assert (valid(i));

            int p = parent(i);
            while ( (p >= 0) && (heap[i].prio < heap[p].prio) ) {
                swap(i, p);
                i = p;
                p = parent(i);
            }
        }

        /**
         * Exchange heap[i] with the child with lowest priority, if any
         * exists, until it reaches the correct position into the heap.
         * This method requires time O(log n).
         */
        private void moveDown(int i)
        {
            assert (valid(i));

            boolean done = false;
            do {
                int dst = minChild(i);
                if (valid(dst) && (heap[dst].prio < heap[i].prio)) {
                    swap(i, dst);
                    i = dst;
                } else {
                    done = true;
                }
            } while (!done);
        }

        /**
         * Insert a new pair (data, prio) into the queue.
         * This method requires time O(log n).
         */
        public void insert(int data, double prio)
        {
            assert ((data >= 0) && (data < maxSize));
            assert (pos[data] == -1);
            assert ( !isFull() );

            final int i = size++;
            pos[data] = i;
            heap[i] = new heapElem(data, prio);
            moveUp(i);
        }

        /**
         * Delete the element with minimum priority. This method requires
         * time O(log n).
         */
        public void deleteMin( )
        {
            assert ( !isEmpty() );

            swap(0, size-1);
            pos[heap[size-1].data] = -1;
            size--;
            if (size>0) moveDown(0);
        }

        /**
         * Chenage the priority associated to |data|. This method requires
         * time O(log n).
         */
        public void changePrio(int data, double newprio)
        {
            int j = pos[data];
            assert ( valid(j) );
            final double oldprio = heap[j].prio;
            heap[j].prio = newprio;
            if (newprio > oldprio) {
                moveDown(j);
            } else {
                moveUp(j);
            }
        }
    }
}

