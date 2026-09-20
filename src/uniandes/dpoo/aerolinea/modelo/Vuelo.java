package uniandes.dpoo.aerolinea.modelo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uniandes.dpoo.aerolinea.exceptions.VueloSobrevendidoException;
import uniandes.dpoo.aerolinea.modelo.cliente.Cliente;
import uniandes.dpoo.aerolinea.modelo.tarifas.CalculadoraTarifas;
import uniandes.dpoo.aerolinea.tiquetes.GeneradorTiquetes;
import uniandes.dpoo.aerolinea.tiquetes.Tiquete;

public class Vuelo
{
    private Ruta ruta;
    private String fecha;
    private Avion avion;
    private Map<String, Tiquete> tiquetes;

    public Vuelo(Ruta ruta, String fecha, Avion avion) {
        this.ruta = ruta;
        this.fecha = fecha;
        this.avion = avion;
        this.tiquetes = new HashMap<String, Tiquete>();
    }

    public Ruta getRuta() {
        return ruta;
    }

    public String getFecha() {
        return fecha;
    }

    public Avion getAvion() {
        return avion;
    }

    public Collection<Tiquete> getTiquetes() {
        return tiquetes.values();
    }

    public void agregarTiquete(Tiquete tiquete) {
        tiquetes.put(tiquete.getCodigo(), tiquete);
    }

    public int venderTiquetes(Cliente cliente, CalculadoraTarifas calculadora, int cantidad) throws VueloSobrevendidoException
    {
        if(cantidad < 0 || tiquetes.size() + cantidad > avion.getCapacidad()) {
            throw new VueloSobrevendidoException(this);
        }

        int tarifa = calculadora.calcularTarifa(this, cliente);
        for(int i = 0; i < cantidad; i++) {
            GeneradorTiquetes.generarTiquete(this, cliente, tarifa);
        }
        return tarifa * cantidad;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof Vuelo)) {
            return false;
        }

        Vuelo otro = (Vuelo)obj;
        return ruta.equals(otro.ruta) && fecha.equals(otro.fecha);
    }
}
