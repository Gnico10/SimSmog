package com.example.simulacion2019;

import android.os.AsyncTask;
import android.widget.ProgressBar;

public final class SimSmog {
    private int d = 30; //Día que se itera.
    private int Pa = 393384; //Parque automortor tomado.
    private double u;
    private double CCO = 0, CNox = 0, CSO2 = 0, CPM = 0, CCO2 = 0;

    //Contadores de tipos de vehículos.
    private int Anaf = 0, Agas = 0, Agnc = 0;
    private int Vnaf = 0, Vgas = 0, VCgas = 0;
    private int Ogas = 0;

    //Variables acumuladoras de gases emitidos medidos en g/km por vehículo.
    private double CO = 0, Nox = 0, SO2 = 0, PM = 0, CO2 = 0;

    static final private boolean debugging = true;


    private static GenPseudAleat rnd;
    private DistribucionesDeProbabilidad dist;

    public SimSmog(GenPseudAleat random) {
        rnd = random;
        dist = new DistribucionesDeProbabilidad(rnd);
    }

    static private class DistribuciónVehículosPHora {
        private int hora;
        private int Anaf = 0, Agas = 0, Agnc = 0;
        private int Vnaf = 0, Vgas = 0, VCgas = 0;
        private int Ogas = 0;

        public DistribuciónVehículosPHora(int hora) {
            this.hora = hora;
        }

        public DistribuciónVehículosPHora(int hora, int anaf, int agas, int agnc, int vnaf, int vgas, int vCgas, int ogas) {
            this.hora = hora;
            Anaf = anaf;
            Agas = agas;
            Agnc = agnc;
            Vnaf = vnaf;
            Vgas = vgas;
            VCgas = vCgas;
            Ogas = ogas;
        }

        public int getHora() {
            return hora;
        }

        public void setHora(int hora) {
            this.hora = hora;
        }

        public int getAnaf() {
            return Anaf;
        }

        public void setAnaf(int anaf) {
            Anaf = anaf;
        }

        public int getAgas() {
            return Agas;
        }

        public void setAgas(int agas) {
            Agas = agas;
        }

        public int getAgnc() {
            return Agnc;
        }

        public void setAgnc(int agnc) {
            Agnc = agnc;
        }

        public int getVnaf() {
            return Vnaf;
        }

        public void setVnaf(int vnaf) {
            Vnaf = vnaf;
        }

        public int getVgas() {
            return Vgas;
        }

        public void setVgas(int vgas) {
            Vgas = vgas;
        }

        public int getVCgas() {
            return VCgas;
        }

        public void setVCgas(int VCgas) {
            this.VCgas = VCgas;
        }

        public int getOgas() {
            return Ogas;
        }

        public void setOgas(int ogas) {
            Ogas = ogas;
        }

        @Override
        public String toString() {
            return "DistribuciónVehículosPHora{" +
                    "hora=" + hora +
                    ", Anaf=" + Anaf +
                    ", Agas=" + Agas +
                    ", Agnc=" + Agnc +
                    ", Vnaf=" + Vnaf +
                    ", Vgas=" + Vgas +
                    ", VCgas=" + VCgas +
                    ", Ogas=" + Ogas +
                    '}';
        }
    }

    static public class CalcularContaminación extends AsyncTask<Integer, DistribuciónVehículosPHora, DistribuciónVehículosPHora[]> {

        DistribuciónVehículosPHora[] vectorVehiculosPHora;
        Long demora;
        ProgressBar pbar_aleatorios;//ProgressBar que muestra el estado de los números aleatorios
        ProgressBar pbar_estado;//ProgressBar que muestra el progreso de la simulación

        //Este método habría que cambiarlo por BroadcastReceiver con onHandleIntent
        //Explicación -> https://stackoverflow.com/a/25232563

        public void setPbar_aleatorios(ProgressBar pbar_aleatorios) {
            this.pbar_aleatorios = pbar_aleatorios;
        }

        public void setPbar_estado(ProgressBar pbar_estado) {
            this.pbar_estado = pbar_estado;
        }

        @Override
        protected DistribuciónVehículosPHora[] doInBackground(Integer... cantidad_horas) {

            vectorVehiculosPHora = new DistribuciónVehículosPHora[cantidad_horas[0]];

            for (int hora = 0; hora < cantidad_horas[0]; hora++) {

                int vehículos_totales = calcCantSF(hora % 24) + calcCantB(hora % 24) + calcCantMP(hora % 24) + calcCantA(hora % 24);

                int Anaf = 0, Agas = 0, Agnc = 0;
                int Vnaf = 0, Vgas = 0, VCgas = 0;
                int Ogas = 0;

                for (int v = 0; v < vehículos_totales; v++) {
                    double u = rnd.getNextPseudoaleatoreo();

                    if (u <= 0.371) { // Automovil a nafta.
                        Anaf++;
                    } else if (u <= 0.641) { // Automovil a Gasoil
                        Agas++;
                    } else if (u <= 0.757) { // Automovil a GNC.
                        Agnc++;
                    } else if (u <= 0.879) { // Vehículo utilitatio liviano a Nafta.
                        Vnaf++;
                    } else if (u <= 0.946) { // Vehículos utilitarios livianos a Gasoil.
                        Vgas++;
                    } else if (u <= 0.994) { // Vehículos de carga a Gasoil.
                        VCgas++;
                    } else { // Ómnibus a Gasoil.
                        Ogas++;
                    }
                }

                vectorVehiculosPHora[hora] =  new DistribuciónVehículosPHora(hora,Anaf,Agas,Agnc,Vnaf,Vgas,VCgas,Ogas);

                publishProgress(vectorVehiculosPHora[hora]);
            }

            return new DistribuciónVehículosPHora[1];
        }

        @Override
        protected void onProgressUpdate(DistribuciónVehículosPHora... values) {
            Mostrar(values[0].toString());
            try{
                pbar_aleatorios.setProgress(rnd.getCantidadGenerados()-rnd.getCantidadUsados());
                pbar_estado.setProgress(values[0].getHora());
            }catch (NullPointerException ignored){

            }
        }

        @Override
        protected void onPostExecute(DistribuciónVehículosPHora[] result) {
            Mostrar("Se ha demorado: "+(System.currentTimeMillis()-demora) +" simulando "+result.length+" horas.");
        }

        @Override
        protected void onPreExecute() {

            demora = System.currentTimeMillis();
        }
    }

    public String simular(int dias, int ParqueAct) {
        d = dias;
        Pa = ParqueAct;

        for (int dia = 1; dia <= d; dia++) {
            simularDia();
        }

        calContaminante();

        if (CNox <= (67000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (88000 * Math.pow(110, 2) * 20) &&
                CPM <= (16000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (9 * Math.pow(110, 2) * 20) &&
                CCO <= (4400 * Math.pow(110, 2) * 20)) {

            //Nivel 1
            return "Nivel: 1 \n" +
                    "Banda: Bajo \n" +
                    "Personas en riesgo: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Población gral: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Las concentraciones de gases nosivos emitidos fue de: \n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (134000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (177000 * Math.pow(110, 2) * 20) &&
                CPM <= (33000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (9 * Math.pow(110, 2) * 20) &&
                CCO <= (9400 * Math.pow(110, 2) * 20)) {

            //Nivel 2
            return "Nivel: 2 \n" +
                    "Banda: Bajo \n" +
                    "Personas en riesgo: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Población gral: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox >= (200000 * Math.pow(110, 2) * 20) &&
                CSO2 >= (266000 * Math.pow(110, 2) * 20) &&
                CPM >= (50000 * Math.pow(110, 2) * 20) &&
                CCO2 >= (9 * Math.pow(110, 2) * 20) &&
                CCO >= (12400 * Math.pow(110, 2) * 20)) {

            //Nivel 3
            return "Nivel: 3 \n" +
                    "Banda: Alto \n" +
                    "Personas en riesgo: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Población gral: Disfruta de tus actividades habituales al aire libre." +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (267000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (354000 * Math.pow(110, 2) * 20) &&
                CPM <= (58000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (9 * Math.pow(110, 2) * 20) &&
                CCO <= (15400 * Math.pow(110, 2) * 20)) {

            //Nivel 4
            return "Nivel: 4 \n" +
                    "Banda: Moderado \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, y adultos con problemas " +
                    "pulmonares/ cardíacos, que experimentan síntomas, deben considerar " +
                    "reducir la actividad física extenuante, especialmente al aire libre.\n" +
                    "Población gral: Disfruta de tus actividades habituales al aire libre." +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (334000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (443000 * Math.pow(110, 2) * 20) &&
                CPM <= (66000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (9 * Math.pow(110, 2) * 20) &&
                CCO <= (30400 * Math.pow(110, 2) * 20)) {

            //Nivel 5
            return "Nivel: 5 \n" +
                    "Banda: Moderado \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, y adultos con problemas " +
                    "pulmonares/ cardíacos, que experimentan síntomas, deben considerar " +
                    "reducir la actividad física extenuante, especialmente al aire libre.\n" +
                    "Población gral: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (400000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (532000 * Math.pow(110, 2) * 20) &&
                CPM <= (75000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (9 * Math.pow(110, 2) * 20) &&
                CCO <= (30400 * Math.pow(110, 2) * 20)) {

            //Nivel 6
            return "Nivel: 6 \n" +
                    "Banda: Moderado \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, y adultos con problemas " +
                    "pulmonares/ cardíacos, que experimentan síntomas, deben considerar " +
                    "reducir la actividad física extenuante, especialmente al aire libre.\n" +
                    "Población gral: Disfruta de tus actividades habituales al aire libre.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (467000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (710000 * Math.pow(110, 2) * 20) &&
                CPM <= (83000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (20 * Math.pow(110, 2) * 20) &&
                CCO <= (40400 * Math.pow(110, 2) * 20)) {

            //Nivel 7
            return "Nivel: 7 \n" +
                    "Banda: Alto \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, y adultos con problemas " +
                    "pulmonares/cardíacos, deben reducir el esfuerzo físico intenso, " +
                    "principalmente al aire libre, y especialmente si experimentan síntomas. " +
                    "Las personas con asma pueden encontrar que necesitan usar su inhalador de " +
                    "alivio con más frecuencia. Las personas mayores también deben reducir el esfuerzo físico.\n" +
                    "Población gral: Cualquier persona que experimente molestias como dolor en los ojos, " +
                    "tos o dolor de garganta debe considerar reducir la actividad, particularmente al aire libre.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (534000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (887000 * Math.pow(110, 2) * 20) &&
                CPM <= (91000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (30 * Math.pow(110, 2) * 20) &&
                CCO <= (40400 * Math.pow(110, 2) * 20)) {

            //Nivel 8
            return "Nivel: 8 \n" +
                    "Banda: Alto \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, y adultos con problemas " +
                    "pulmonares/cardíacos, deben reducir el esfuerzo físico intenso, " +
                    "principalmente al aire libre, y especialmente si experimentan síntomas. " +
                    "Las personas con asma pueden encontrar que necesitan usar su inhalador de " +
                    "alivio con más frecuencia. Las personas mayores también deben reducir el esfuerzo físico.\n" +
                    "Población gral: Cualquier persona que experimente molestias como dolor en los ojos, " +
                    "tos o dolor de garganta debe considerar reducir la actividad, particularmente al aire libre.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else if (CNox <= (600000 * Math.pow(110, 2) * 20) &&
                CSO2 <= (1064000 * Math.pow(110, 2) * 20) &&
                CPM <= (100000 * Math.pow(110, 2) * 20) &&
                CCO2 <= (40 * Math.pow(110, 2) * 20) &&
                CCO <= (40400 * Math.pow(110, 2) * 20)) {

            //Nivel 9
            return "Nivel: 9 \n" +
                    "Banda: Alto \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, y adultos con problemas " +
                    "pulmonares/cardíacos, deben reducir el esfuerzo físico intenso, " +
                    "principalmente al aire libre, y especialmente si experimentan síntomas. " +
                    "Las personas con asma pueden encontrar que necesitan usar su inhalador de " +
                    "alivio con más frecuencia. Las personas mayores también deben reducir el esfuerzo físico.\n" +
                    "Población gral: Cualquier persona que experimente molestias como dolor en los ojos, " +
                    "tos o dolor de garganta debe considerar reducir la actividad, particularmente al aire libre.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        } else {
            //Nivel 10
            return "Nivel: 10 \n" +
                    "Banda: Muy Alto \n" +
                    "Personas en riesgo: Los niños con problemas pulmonares, adultos con problemas pulmonares/ cardíacos " +
                    "y las personas mayores de edad deben evitar la actividad física extenuante. Las personas " +
                    "con asma pueden encontrar que necesitan usar su inhalador de alivio con más frecuencia.\n" +
                    "Población gral: Reduzca el esfuerzo físico, especialmente al aire libre, especialmente si experimenta síntomas" +
                    "como tos o dolor de garganta.\n" +
                    "Concentración de Nox=" + CNox + "\n" +
                    "Concentración de SO2=" + CSO2 + "\n" +
                    "Concentración de PM=" + CPM + "\n" +
                    "Concentración de CO2=" + CCO2 + "\n" +
                    "Concentración de CO=" + CCO;

        }
    }

    public void simularDia() {
        double Pd = 0; // Crecimiento diario del parque automotor

        //Generación de un numero aleatoreo.
        u = rnd.getNextPseudoaleatoreo();

        if (u <= 0.099) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 23 + 16 * u;
        } else if (u <= 0.414) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 55 + 16 * u;
        } else if (u <= 0.711) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 39 + 16 * u;
        } else if (u <= 0.855) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 55 + 16 * u;
        } else if (u <= 0.954) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 87 + 16 * u;
        } else if (u <= 0.981) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 103 + 16 * u;
        } else if (u <= 0.982) {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 119 + 16 * u;
        } else {
            u = rnd.getNextPseudoaleatoreo();
            Pd = 135 + 16 * u;
        }

        Pa = (int) Pd + Pa; //Crecimiento del parque automotor en el día.

        simularHora();
    }

    private static int calcCantSF(int hora) {
        return (int) (64 * (rnd.getNextPseudoaleatoreo() + 0.5));//(int) ((Pa / 6250) * (-0.0001735583 * Math.pow(hora, 3) + 0.0006015723 * Math.pow(hora, 2) + 0.1024126236));
    }

    private static int calcCantB(int hora) {
        return (int) (143 * (rnd.getNextPseudoaleatoreo() + 0.5));//(int) ((Pa / 2777) * (-0.0000187516 * Math.pow(t, 5) + 0.0011141716 * Math.pow(t, 4) - 0.0239325241 * Math.pow(t, 3) + 0.218239282 * Math.pow(t, 2) - 0.6962625685 * t + 0.8311836904));
    }

    private static int calcCantMP(int hora) {
        return (int) (210 * (rnd.getNextPseudoaleatoreo() + 0.5));//(int) ((Pa / 1887) * (-0.0000011697 * Math.pow(t, 6) + 0.0000643631 * Math.pow(t, 5) - 0.0011292477 * Math.pow(t, 4) + 0.0046414858 * Math.pow(t, 3) + 0.0460776330 * Math.pow(t, 2) + 0.2762486787 * t + 0.5339823064));
    }

    private static int calcCantA(int hora) {
        return (int) (423 * (rnd.getNextPseudoaleatoreo() + 0.5));//(int) ((Pa / 934) * (-0.0003407824 * Math.pow(t, 3) + 0.0070874777 * Math.pow(t, 2) + 0.0356608971 * t + 0.1182947487));
    }

    private void simularHora() {
        // Calculo del caudal de vehículos que pasan por cada calle tenída en cuenta por cada hora del día.
        // Funcion polinomial de tendencia de datos de Google Maps.
        for (int t = 1; t <= 24; t++) {
            int Csf = calcCantSF(t);
            int Cb = calcCantB(t);
            int Cmp = calcCantMP(t);
            int Ca = calcCantA(t);

            actContaminante(Csf); // Actualización de concentación de contaminantes para la calle Santa Fé.
            actContaminante(Cb); // Actualización de concentación de contaminantes para la calle Balcarce.
            actContaminante(Cmp); // Actualización de concentación de contaminantes para la calle Marcos Páz.
            actContaminante(Ca); // Actualización de concentación de contaminantes para la calle Avellaneda.
        }
    }

    private void actContaminante(int vehCalle) {
        // Ciclo por cada vehículo que pasa en cada calle.
        for (int v = 1; v <= vehCalle; v++) {

            u = rnd.getNextPseudoaleatoreo();

            if (u <= 0.371) { // Automovil a nafta.
                Anaf++;
                CO += 11.09;
                Nox += 0.74;
                SO2 += 0.07;
                PM += 0.08;
                CO2 += 196;
            } else if (u <= 0.641) { // Automovil a Gasoil
                Agas++;
                CO += 2.60;
                Nox += 2.30;
                SO2 += 1.52;
                PM += 0.34;
                CO2 += 484;
            } else if (u <= 0.757) { // Automovil a GNC.
                Agnc++;
                CO += 4;
                Nox += 2;
                SO2 += 0;
                PM += 0.01;
                CO2 += 190;
            } else if (u <= 0.879) { // Vehículo utilitatio liviano a Nafta.
                Vnaf++;
                CO += 18.80;
                Nox += 1.03;
                SO2 += 0.07;
                PM += 0.07;
                CO2 += 217.57;
            } else if (u <= 0.946) { // Vehículos utilitarios livianos a Gasoil.
                Vgas++;
                CO += 2.60;
                Nox += 2.30;
                SO2 += 1.52;
                PM += 0.34;
                CO2 += 484;
            } else if (u <= 0.994) { // Vehículos de carga a Gasoil.
                VCgas++;
                CO += 3.10;
                Nox += 2.70;
                SO2 += 1.82;
                PM += 0.22;
                CO2 += 580;
            } else { // Ómnibus a Gasoil.
                Ogas++;
                CO += 14.20;
                Nox += 10.28;
                SO2 += 0.14;
                PM += 0.52;
                CO2 += 1197;
            }
        }
    }

    private void calContaminante() {
        // Modelo de distribución de contaminantes de celda fija estacionaria.
        double Vt = dist.normal(14, 5); // (Km / h)
        Vt = Vt * 1000; // Pasaje a (m / h)

        // Concentraciones de contaminantes en (mg).
        CCO = (CO * 110) / (Vt * 20);
        CNox = (Nox * 110) / (Vt * 20);
        CSO2 = (SO2 * 110) / (Vt * 20);
        CPM = (PM * 110) / (Vt * 20);
        CCO2 = (CO2 * 110) / (Vt * 20);
    }

    static private void Mostrar(String texto){
        if(debugging){
            System.out.println("En SimSmog -> "+ texto);
        }
    }
}
