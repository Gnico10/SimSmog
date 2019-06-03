package com.example.simulacion2019;

public class DistribuciónesDeProbabilidad {

    public static double exponencial(double esperado){
        return -esperado * Math.log(Math.random());
    }

    public static double poisson(double alfa){
        //alfa es la cantidad de eventos por continuo (unidad de tiempo)

        double b = Math.exp(-alfa);

        int x=0;

        double p=1;
        while(p>b){
            double u = Math.random();
            p=p*u;
            x=x+1;
        }

        return x;

    }

    public static double Normal(double media,double desvio){
        double sum =0;
        for (int i=0;i<12;i++){
            sum = sum + Math.random();
        }
        return desvio * (sum - 6) + media;

    }
}
