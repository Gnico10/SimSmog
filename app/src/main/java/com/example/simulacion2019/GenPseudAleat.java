package com.example.simulacion2019;

import android.os.AsyncTask;

import java.util.Arrays;

public class GenPseudAleat {
    private long semilla;
    private long last;
    private int a;
    private int c;
    private int m;
    static private MainActivity main;
    static final private int TAMANIOVECTOR = 15000;
   static final private boolean debugging = false;

    private double[] random = new double[TAMANIOVECTOR];
    private boolean generando = false;

    private static final double Dn = 0.055;
    private int cantidadUsados = 0;
    private int cantidadGenerados = 0;

    /*
        Clase para la ejecución de una rutina que genera números pseudoaleatoreos
        en un hilo de ejecución paralelos al hilo principal.
    */
    static private class GenerateAsync extends AsyncTask<GenPseudAleat, Void, double[]> {
        private long semilla;
        private long last;
        private int a;
        private int c;
        private int m;
        private GenPseudAleat poolLocation;


        private double[] generateValid(int tamanio) {
            // validación de los números pseudoaleatorios por prueba de Kolmogorov - Smirnov (K-S)
            double[] aleatorios;
            int loop = 0;
            int multip = 0;

            do {
                // 1) Generación de los números pseudoaleatorios.
                aleatorios = generate(tamanio);

                loop = loop + 1;

                //Se ajusta el dnalfa para una comprobación más exigente.
                if (loop % 500 == 0) {
                    multip = multip + 1;
                }
            }
            while (validate(aleatorios, Dn - (0.005 * multip)));


            return aleatorios;
        } // Generación de números pseudoaleatorios validos por prueba (K-S)

        private double[] generate(int tamanio) {
            double[] valores = new double[tamanio];

            for (int i = 0; i < tamanio; i++) {
                valores[i] = generateOne();
            }
            return valores;
        } // Genera una lista de números pseudoaleatoreos.

        private double generateOne() {
            // Método congruencial mixto.
            last = (a * last + c) % m;
            float resultado = last;
            resultado = (resultado % 1000000) / 1000000;

            return (double) resultado;
        } // Genera el siguiente número pseaudoaleatoreo.

        private boolean validate(double[] entrada, double dnAlfa) {
            // validación de los números pseudoaleatorios por prueba de Kolmogorov - Smirnov (K-S).
            double[] ordenado = entrada.clone();
            double acumulada;
            double diferencia;

            double Dn = (double) 0;

            // 2) Los números generados son ordenados.
            Arrays.sort(ordenado);
            for (int i = 0; i < ordenado.length; i++) {
                // 3) Se calcula la distribución acumulada por  cada uno de los números generados.
                acumulada = ((double) i / (double) entrada.length);

                // 4) Se calcula el estadistico K-S recorriendo cada uno de los números y quedandose
                // con la diferencia entre el acumulado correspondiente y el número aleatoreo.
                diferencia = Math.abs(acumulada - ordenado[i]);

                if (diferencia > Dn) {
                    Dn = diferencia;
                }
            }
            // Retorna si pasa o no la prueba.
            return Dn < dnAlfa;
        }

        @Override
        protected double[] doInBackground(GenPseudAleat... params) {

            if (params[0] != null) {
                poolLocation = params[0];

                semilla = last = System.currentTimeMillis();
                a = (int) (semilla % 100000);
                c = (int) (semilla % 125000);
                m = a + c + (int) (semilla % 5000000);

                // Manda a generar 1000 nuevos números pseudoaleatoreos. El Dn está determinado como
                // el valor promedio para 1000, por lo que si se cambia el tiempo varía significativamente.
                return generateValid(1000);
            }
            return null;
        }

        @Override
        protected void onPostExecute(double[] result) {
            // Manda a guardarse los números generados.
            poolLocation.addToPool(result);

            Mostrar("Se generaron 1000 números");

            if (result != null & poolLocation != null) {
                poolLocation.setGenerando(false);
            }

            if (poolLocation.esNecesarioGenerar()) {
                new GenerateAsync().execute(poolLocation);
            }


        }

        @Override
        protected void onPreExecute() {
            if (poolLocation != null) {
                poolLocation.setGenerando(true);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public GenPseudAleat(MainActivity main) {
        this.main = main;
        generateAsync();
        semilla = last = System.currentTimeMillis();
        a = (int) (semilla % 100000);
        c = (int) (semilla % 125000);
        m = a + c + (int) (semilla % 5000000);
    }

    public boolean esNecesarioGenerar() {
        try {
            return (cantidadGenerados - cantidadUsados < (TAMANIOVECTOR / 3) & !generando);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getNextPseudoaleatoreo() {
        // Devuelve un valor de entre todos los generados.

        double devol;
        cantidadUsados++;

        // Generado nuevos números aleatoreos si es necesario.
        if (esNecesarioGenerar()) {
            generando = true;
            new GenerateAsync().execute(this);
        }
        double rand = Math.random();
        if (rand > 0.999) {
            Mostrar("\nCantidad de usados: " + cantidadUsados);
            Mostrar("\ncantidadGenerados: " + cantidadGenerados);
        }


        //  ProgressBar progBar = main.findViewById(R.id.progressBar_rand);
        // progBar.setProgress(progBar.getProgress() - 1);

        // En caso de no haber números aleatoreos, son generados y luego devueltos. esto debe ser un valor chico
        // para no retrasar tanto la simulación
        if (cantidadGenerados <= cantidadUsados) {
            Mostrar("\n\nGenerando 100\n\n");
            addToPool(generateValid((TAMANIOVECTOR / 10)));
        }

        // random es una lista de números aleatores guardados con anterioridad.
        return random[cantidadUsados % TAMANIOVECTOR];

    }

    public void generateAsync() {
        // Lanza un nuevo hilo de ejecuación para la generación de números pseudoaleatoreos.
        new GenerateAsync().execute(this);

    }

    public int getCantidadUsados() {
        return cantidadUsados;
    }

    public int getCantidadGenerados() {
        return cantidadGenerados;
    }

    private void addToPool(double[] nuevos) {
        // Agrega los números aleatoreos generados y validados a la lista.
        int contador = 0;
        cantidadGenerados = cantidadGenerados + nuevos.length;
        for (int i = cantidadGenerados; i < cantidadGenerados + nuevos.length - 1; i++) {
            random[i % TAMANIOVECTOR] = nuevos[contador];
            contador++;
        }

        // ProgressBar progBar = main.findViewById(R.id.progressBar_rand);
        // progBar.setProgress(progBar.getProgress() + nuevos.length);
    }

    private void setGenerando(boolean generando) {
        this.generando = generando;
    }

    // Metodos para una ejecución sincrónica de números pseudoaleatorios en caso de quedarse
    // sin números y estar generando en segundo plano.
    // Métodos iguales a los de la clase asincrona.
    private double[] generateValid(int tamanio) {
        double[] aleatorios;
        int loop = 0;
        int multip = 0;
        do {
            aleatorios = generate(tamanio);

            loop = loop + 1;

            if (loop % 500 == 0) {
                multip = multip + 1;
            }
        }
        while (validate(aleatorios, Dn - (0.005 * multip)));

        return aleatorios;
    }

    private double generateOne() {
        // Método congruencial mixto.
        last = (a * last + c) % m;
        float resultado = last;
        resultado = (resultado % 1000000) / 1000000;

        return (double) resultado;

    } // Genera el siguiente número pseaudoaleatoreo.

    private double[] generate(int tamanio) {
        // Metodo congruencial mixto para generación de números pseudoaleatoreos.
        // Semilla generada a partir de la hora en milisegundos.
        semilla = last = System.currentTimeMillis();
        a = (int) (semilla % 100000);
        c = (int) (semilla % 125000);
        m = a + c + (int) (semilla % 5000000);
        double[] valores = new double[tamanio];

        for (int i = 0; i < tamanio; i++) {
            // Se guardan tantos valores como se indicó que se generaran.
            valores[i] = generateOne();
        }
        return valores;
    } // Generación de números pseudoaleatoreos con el método congruencial mixto.

    private boolean validate(double[] entrada, double dnAlfa) {
        // validación de los números pseudoaleatorios por prueba de Kolmogorov - Smirnov (K-S).
        double[] ordenado = entrada.clone();
        double acumulada;
        double diferencia;

        double Dn = (double) 0;

        Arrays.sort(ordenado);
        for (int i = 0; i < ordenado.length; i++) {

            acumulada = ((double) i / (double) entrada.length);
            diferencia = Math.abs(acumulada - ordenado[i]);

            if (diferencia > Dn) {
                Dn = diferencia;
            }
        }
        return Dn < dnAlfa;
    }

    @Override
    public String toString() {
        return "GenPseudAleat{" +
                "random=" + Arrays.toString(random) +
                ", generando=" + generando +
                ", cantidadUsados=" + cantidadUsados +
                ", cantidadGenerados=" + cantidadGenerados +
                '}';
    }

     static private void Mostrar(String texto){
        if(debugging){
            System.out.println("En GenPseudAleat -> "+ texto);
        }
    }
}
