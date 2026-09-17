package com.teletubies.endpoints.cotizacion.dto;


public class CotizacionesResponso {
    private String origen;
    private String destino;
    private double tarifa;
    private double pesoKG;
    private double costoEstimado;
    private double costoTotal;


    public CotizacionesResponso( String origen, String destino, double tarifa, double pesoKG, double costoEstimado, double costoTotal) {
        this.origen = origen;
        this.destino = destino;
        this.tarifa = tarifa;
        this.pesoKG = pesoKG;
        this.costoEstimado = costoEstimado;
        this.costoTotal = costoTotal;

    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public double getTarifa() {
        return tarifa;
    }

    public double getPesoKG() {
        if (pesoKG <= 50) {
            System.out.println("peso valido");
            return pesoKG;
        }
        if (pesoKG > 50 && pesoKG <= 70) {
            System.out.println("peso valido con costo adicional");
            return pesoKG;
        }
        if (pesoKG > 70) {
            throw new IllegalArgumentException("El peso no puede ser mayor a 70 kg");
        }
        throw new IllegalArgumentException("Peso no válido");
    }



    public double getCostoEstimado() {
        return costoEstimado;
    }

    public double getCostoTotal() {
        return costoTotal;
    }



}