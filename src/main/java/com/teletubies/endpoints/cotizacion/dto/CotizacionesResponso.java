package com.teletubies.endpoints.cotizacion.dto;


import lombok.EqualsAndHashCode;
import lombok.ToString;


@ToString
@EqualsAndHashCode
public class CotizacionesResponso {

    private final String origen;
    private final String destino;
    private final double tarifa;
    private final double pesoKG;
    private final double costoEstimado;
    private final double costoTotal;


    public CotizacionesResponso( String origen, String destino, double tarifa, double pesoKG, double costoEstimado, double costoTotal) {
        this.origen = origen;
        this.destino = destino;
        this.tarifa = tarifa;
        this.pesoKG = pesoKG;
        this.costoEstimado = costoEstimado;
        this.costoTotal = costoTotal;

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





}