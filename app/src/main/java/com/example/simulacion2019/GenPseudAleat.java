package com.example.simulacion2019;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;

public class GenPseudAleat {
    private long semilla;
    private long last;
    private int a;
    private int c;
    private int m;
    private ArrayList<Double> random;
    private boolean generando = false;
    private static final double Dn = 0.055;

    /*
        Clase para la ejecución de una rutina que genera números pseudoaleatoreos
        en un hilo de ejecución paralelos al hilo principal.
    */
    static private class GenerateAsync extends AsyncTask<GenPseudAleat, Void, Double[]> {
        private long semilla;
        private long last;
        private int a;
        private int c;
        private int m;
        private GenPseudAleat poolLocation;

        private Double[] generateValid(int tamanio) {
            // validación de los números pseudoaleatorios por prueba de Kolmogorov - Smirnov (K-S)
            Double[] aleatorios;
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

            System.out.println("\n\n\nCantidad de secuencias generadas: " + loop + "\nCon un DnAlfa=" +
                                (Dn - (0.01 * multip)) + "\n\n\n");

            return aleatorios;
        } // Generación de números pseudoaleatorios validos por prueba (K-S)

        private Double[] generate(int tamanio) {
            Double[] valores = new Double[tamanio];

            for (int i = 0; i < tamanio; i++) {
                valores[i] = generateOne();
            }
            //  System.out.println(" valores: "+Arrays.toString(valores));
            return valores;
        } // Genera una lista de números pseudoaleatoreos.

        private Double generateOne() {
            // Método congruencial mixto.
            last = (a * last + c) % m;
            float resultado = last;
            resultado = (resultado % 1000000) / 1000000;

            return (double) resultado;
        } // Genera el siguiente número pseaudoaleatoreo.

        private boolean validate(Double[] entrada, Double dnAlfa) {
            // validación de los números pseudoaleatorios por prueba de Kolmogorov - Smirnov (K-S).
            Double[] ordenado = entrada.clone();
            Double acumulada;
            Double diferencia;

            Double Dn = (double) 0;

            // 2) Los números generados son ordenados.
            Arrays.sort(ordenado);
            for (int i = 0; i < ordenado.length; i++) {
                // 3) Se calcula la distribución acumulada por  cada uno de los números generados.
                acumulada = ((double) i / (double) entrada.length);

                // 4) Se calcula el estaduistico K-S recorriendo cada uno de los números y quedandose con la mayor
                // diferencia entre el acumulado correspondiente y el número aleatoreo.
                diferencia = Math.abs(acumulada - ordenado[i]);

                if (diferencia > Dn) {
                    Dn = diferencia;
                }
            }
            //System.out.println(Dn);

            // Retorna si pasa o no la prueba.
            return Dn < dnAlfa;
        }

        @Override
        protected Double[] doInBackground(GenPseudAleat... params) {

            if (params[0] != null) {
                poolLocation = params[0];

                semilla = last = System.currentTimeMillis();
                a = (int) (semilla % 100000);
                c = (int) (semilla % 125000);
                m = a + c + (int) (semilla % 5000000);

                // Manda a generar 10000 nuevos números pseudoaleatoreos.
                return generateValid(10000);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Double[] result) {

            // Manda a guardarse los números generados.
            poolLocation.addToPool(result);
            if (result != null & poolLocation != null) {
                poolLocation.setGenerando(false);
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

    public GenPseudAleat() {
        random = new ArrayList<>();
        generateAsync();
    }

    public Double getNextPseudoaleatoreo() {
        // Devuelve un valor de entre todos los generados.
        Double devol;
        System.out.println(this.toString());
        try {
            // random es una lista de números aleatores guardados con anterioridad.
            devol = random.get(0);
            // Una vez utilizado el número, es borrado.
            random.remove(0);
            // Generado nuevos números aleatoreos si es necesario.
            if (random.size() < 5000 & !generando) {
                generateAsync();
                generando = true;
            }
            return devol;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            random.clear();
            // En caso de no haber números aleatoreos, son generados y luego devueltos.
            addToPool(generateValid(5000));
            return getNextPseudoaleatoreo();
        }
    }

    public void generateAsync() {
        // Lanza un nuevo hilo de ejecuación para la generación de números pseudoaleatoreos.
        new GenerateAsync().execute(this);
    }

    private void addToPool(Double[] nuevos) {
        // Agrega los números aleatoreos generados y validados a la lista.
        for (int i = 0; i < nuevos.length - 1; i++) {
            random.add(nuevos[i]);
        }
    }

    private void setGenerando(boolean generando) {
        this.generando = generando;
    }

    // Metodos para una ejecución sincrónica de números pseudoaleatorios en caso de quedarse
    // sin números y estar generando en segundo plano.
    // Métodos iguales a los de la clase asincrona.
    private Double[] generateValid(int tamanio) {
        Double[] aleatorios;
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

        System.out.println("\n\n\nCantidad de secuencias generadas: " + loop +
                            "\nCon un DnAlfa=" + (Dn - (0.01 * multip)) + "\n\n\n");

        return aleatorios;
    }

    private Double generateOne() {
        // Método congruencial mixto.
        last = (a * last + c) % m;
        float resultado = last;
        resultado = (resultado % 1000000) / 1000000;

        return (double) resultado;

    } // Genera el siguiente número pseaudoaleatoreo.

    private Double[] generate(int tamanio) {
        // Metodo congruencial mixto para generación de números pseudoaleatoreos.
        // Semilla generada a partir de la hora en milisegundos.
        semilla = last = System.currentTimeMillis();
        a = (int) (semilla % 100000);
        c = (int) (semilla % 125000);
        m = a + c + (int) (semilla % 5000000);
        Double[] valores = new Double[tamanio];

        for (int i = 0; i < tamanio; i++) {
            // Se guardan tantos valores como se indicó que se generaran.
            valores[i] = generateOne();
        }
        //  System.out.println(" valores: "+Arrays.toString(valores));
        return valores;
    } // Generación de números pseudoaleatoreos con el método congruencial mixto.

    private boolean validate(Double[] entrada, Double dnAlfa) {
        // validación de los números pseudoaleatorios por prueba de Kolmogorov - Smirnov (K-S).
        Double[] ordenado = entrada.clone();
        Double acumulada;
        Double diferencia;

        Double Dn = (double) 0;

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
                "\nSize=" + random.size() +
                //"\nrandom=" + random +
                '}';
    }
}
