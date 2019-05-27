package com.example.simulacion2019;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;

public class GenPseudAleat {

    private ArrayList<Double> random;
    private long semilla;
    private long last;
    private int a;
    private int c;
    private int m;
    private boolean generando = false;
    private static final double Dn = 0.055;


    static private class GenerateAsync extends AsyncTask<GenPseudAleat, Void, Double[]> {
        private long semilla;
        private long last;
        private int a;
        private int c;
        private int m;
        private GenPseudAleat poolLocation;

        private Double generateOne() {

            last = (a * last + c) % m;
            float resultado = last;
            resultado = (resultado % 1000000) / 1000000;

            return (double) resultado;

        }

        private Double[] generate(int tamanio) {
            //semilla = last = System.currentTimeMillis();
            Double[] valores = new Double[tamanio];

            for (int i = 0; i < tamanio; i++) {
                valores[i] = generateOne();
            }
            //  System.out.println(" valores: "+Arrays.toString(valores));
            return valores;

        }

        private Double[] generateValid(int tamanio) {
            Double[] aleatorios;
            int i = 0;
            int multip = 0;
            do {
                aleatorios = generate(tamanio);

                i = i + 1;

                if (i % 500 == 0) {
                    multip = multip + 1;
                }
            }
            while (validate(aleatorios, Dn - (0.005 * multip)));

            System.out.println("\n\n\nCantidad de secuencias generadas: " + i + "\nCon un Dn=" + (Dn - (0.01 * multip)) + "\n\n\n");

            return aleatorios;


        }

        private boolean validate(Double[] entrada, Double dnAlfa) {

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
            //System.out.println(Dn);
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

                return generateValid(1000);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Double[] result) {

            poolLocation.addToPool(result);
            if (result != null & poolLocation != null) {
                poolLocation.setGenerando(false);
            }

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
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

    }

    public void addToPool(Double[] nuevos) {
        for (int i = 0; i < nuevos.length - 1; i++) {
            random.add(nuevos[i]);
        }
    }

    public void generateAsync() {
        new GenerateAsync().execute(this);
    }

    public Double get() {
        Double devol;
        System.out.println(this.toString());
        try {
            devol = random.get(0);
            random.remove(0);
            if (random.size() < 5000 & !generando) {
                new GenerateAsync().execute(this);
                generando = true;
            }
            return devol;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            random.clear();
            addToPool(generateValid(25));
            return get();
        }


    }

    public Double generateOne() {

        last = (a * last + c) % m;
        float resultado = last;
        resultado = (resultado % 1000000) / 1000000;

        return (double) resultado;

    }

    public Double[] generate(int tamanio) {
        semilla = last = System.currentTimeMillis();
        a = (int) (semilla % 100000);
        c = (int) (semilla % 125000);
        m = a + c + (int) (semilla % 5000000);
        Double[] valores = new Double[tamanio];

        for (int i = 0; i < tamanio; i++) {
            valores[i] = generateOne();
        }
        //  System.out.println(" valores: "+Arrays.toString(valores));
        return valores;

    }

    public Double[] generateValid(int tamanio) {
        Double[] aleatorios;
        int i = 0;
        int multip = 0;
        do {
            aleatorios = generate(tamanio);

            i = i + 1;

            if (i % 500 == 0) {
                multip = multip + 1;
            }
        }
        while (validate(aleatorios, Dn - (0.005 * multip)));

        System.out.println("\n\n\nCantidad de secuencias generadas: " + i + "\nCon un Dn=" + (Dn - (0.01 * multip)) + "\n\n\n");

        return aleatorios;


    }

    public void testDn(int tamanio) {
        getDn(generate(tamanio));

    }

    public boolean validate(Double[] entrada, Double dnAlfa) {

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
        // System.out.println(Dn);
        return Dn < dnAlfa;
    }

    public void getDn(Double[] entrada) {

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
        // System.out.println(Dn);
        System.out.println(Dn);
    }


    public boolean isGenerando() {
        return generando;
    }

    public void setGenerando(boolean generando) {
        this.generando = generando;
    }

    @Override
    public String toString() {
        return "GenPseudAleat{" +
                "\nSize=" + random.size() +
                //"\nrandom=" + random +
                '}';
    }
}
