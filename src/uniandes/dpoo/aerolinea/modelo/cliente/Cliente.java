package uniandes.dpoo.aerolinea.modelo.cliente;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uniandes.dpoo.aerolinea.modelo.Vuelo;
import uniandes.dpoo.aerolinea.tiquetes.Tiquete;

public abstract class Cliente
{
    private List<Tiquete> tiquetesSinUsar;
    private List<Tiquete> tiquetesUsados;

    public Cliente() {
        tiquetesSinUsar = new ArrayList<Tiquete>();
        tiquetesUsados = new ArrayList<Tiquete>();
    }

    public abstract String getTipoCliente();

    public abstract String getIdentificador();

    public void agregarTiquete(Tiquete tiquete) {
        tiquetesSinUsar.add(tiquete);
    }

    public int calcularValorTotalTiquetes() {
        int total = 0;
        for(Tiquete tiquete : tiquetesSinUsar) {
            total += tiquete.getTarifa();
        }
        for(Tiquete tiquete : tiquetesUsados) {
            total += tiquete.getTarifa();
        }
        return total;
    }

    public void usarTiquetes(Vuelo vuelo) {
        Iterator<Tiquete> iterador = tiquetesSinUsar.iterator();
        while(iterador.hasNext()) {
            Tiquete tiquete = iterador.next();
            if(tiquete.getVuelo().equals(vuelo)) {
                tiquete.marcarComoUsado();
                iterador.remove();
                tiquetesUsados.add(tiquete);
            }
        }
    }
}
