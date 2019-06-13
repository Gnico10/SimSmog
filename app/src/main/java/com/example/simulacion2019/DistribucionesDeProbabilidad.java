package com.example.simulacion2019;

public class DistribucionesDeProbabilidad {

    private GenPseudAleat rnd;

    public DistribucionesDeProbabilidad(GenPseudAleat random){
        rnd = random;
    }

    public double normal(double media,double desvio){
        double sum =0;
        double u;

        for (int i = 0; i < 12; i++){
            u = rnd.getNextPseudoaleatoreo();
            sum = sum + u;
        }
        return desvio * (sum - 6) + media;
    }

    public double poisson(double alfa){
        //alfa es la cantidad de eventos por continuo (unidad de tiempo)
        double b = Math.exp(-alfa);

        int x = 0;

        double p=1;
        while(p > b){
            double u = rnd.getNextPseudoaleatoreo();
            p = p * u;
            x = x + 1;
        }

        return x;
    }
}