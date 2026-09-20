package uniandes.dpoo.aerolinea.persistencia;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import uniandes.dpoo.aerolinea.exceptions.AeropuertoDuplicadoException;
import uniandes.dpoo.aerolinea.exceptions.InformacionInconsistenteException;
import uniandes.dpoo.aerolinea.modelo.Aerolinea;
import uniandes.dpoo.aerolinea.modelo.Aeropuerto;
import uniandes.dpoo.aerolinea.modelo.Avion;
import uniandes.dpoo.aerolinea.modelo.Ruta;
import uniandes.dpoo.aerolinea.modelo.Vuelo;

public class PersistenciaAerolineaJson implements IPersistenciaAerolinea
{
    @Override
    public void cargarAerolinea(String archivo, Aerolinea aerolinea) throws IOException, InformacionInconsistenteException
    {
        try
        {
            String contenido = new String(Files.readAllBytes(new File(archivo).toPath()));
            JSONObject raiz = new JSONObject(contenido);
            Map<String, Aeropuerto> aeropuertos = cargarAeropuertos(raiz.getJSONArray("aeropuertos"));

            cargarAviones(raiz.getJSONArray("aviones"), aerolinea);
            cargarRutas(raiz.getJSONArray("rutas"), aeropuertos, aerolinea);
            cargarVuelos(raiz.getJSONArray("vuelos"), aerolinea);
        }
        catch(AeropuertoDuplicadoException e) {
            throw new InformacionInconsistenteException(e.getMessage());
        }
    }

    private Map<String, Aeropuerto> cargarAeropuertos(JSONArray jAeropuertos) throws AeropuertoDuplicadoException, InformacionInconsistenteException
    {
        Map<String, Aeropuerto> aeropuertos = new HashMap<String, Aeropuerto>();
        for(int i = 0; i < jAeropuertos.length(); i++) {
            JSONObject jAeropuerto = jAeropuertos.getJSONObject(i);
            String codigo = jAeropuerto.getString("codigo");
            Aeropuerto aeropuerto = new Aeropuerto(jAeropuerto.getString("nombre"), codigo, jAeropuerto.getString("nombreCiudad"),
                    jAeropuerto.getDouble("latitud"), jAeropuerto.getDouble("longitud"));
            aeropuertos.put(codigo, aeropuerto);
        }
        return aeropuertos;
    }

    private void cargarAviones(JSONArray jAviones, Aerolinea aerolinea) {
        for(int i = 0; i < jAviones.length(); i++) {
            JSONObject jAvion = jAviones.getJSONObject(i);
            aerolinea.agregarAvion(new Avion(jAvion.getString("nombre"), jAvion.getInt("capacidad")));
        }
    }

    private void cargarRutas(JSONArray jRutas, Map<String, Aeropuerto> aeropuertos, Aerolinea aerolinea) throws InformacionInconsistenteException
    {
        for(int i = 0; i < jRutas.length(); i++) {
            JSONObject jRuta = jRutas.getJSONObject(i);
            Aeropuerto origen = aeropuertos.get(jRuta.getString("origen"));
            Aeropuerto destino = aeropuertos.get(jRuta.getString("destino"));
            aerolinea.agregarRuta(new Ruta(origen, destino, jRuta.getString("horaSalida"), jRuta.getString("horaLlegada"),
                    jRuta.getString("codigoRuta")));
        }
    }

    private void cargarVuelos(JSONArray jVuelos, Aerolinea aerolinea) throws InformacionInconsistenteException
    {
        Map<String, Avion> aviones = new HashMap<String, Avion>();
        for(Avion avion : aerolinea.getAviones()) {
            aviones.put(avion.getNombre(), avion);
        }

        for(int i = 0; i < jVuelos.length(); i++) {
            JSONObject jVuelo = jVuelos.getJSONObject(i);
            Ruta ruta = aerolinea.getRuta(jVuelo.getString("codigoRuta"));
            Avion avion = aviones.get(jVuelo.getString("avion"));
            try
            {
                aerolinea.programarVuelo(jVuelo.getString("fecha"), ruta.getCodigoRuta(), avion.getNombre());
            }
            catch(Exception e) {
                throw new InformacionInconsistenteException(e.getMessage());
            }
        }
    }

    @Override
    public void salvarAerolinea(String archivo, Aerolinea aerolinea) throws IOException
    {
        JSONObject raiz = new JSONObject();
        JSONArray jAeropuertos = new JSONArray();
        JSONArray jAviones = new JSONArray();
        JSONArray jRutas = new JSONArray();
        JSONArray jVuelos = new JSONArray();
        Set<String> codigosAeropuertos = new HashSet<String>();

        for(Avion avion : aerolinea.getAviones()) {
            jAviones.put(new JSONObject().put("nombre", avion.getNombre()).put("capacidad", avion.getCapacidad()));
        }

        for(Ruta ruta : aerolinea.getRutas()) {
            agregarAeropuerto(ruta.getOrigen(), codigosAeropuertos, jAeropuertos);
            agregarAeropuerto(ruta.getDestino(), codigosAeropuertos, jAeropuertos);
            jRutas.put(new JSONObject().put("codigoRuta", ruta.getCodigoRuta()).put("origen", ruta.getOrigen().getCodigo())
                    .put("destino", ruta.getDestino().getCodigo()).put("horaSalida", ruta.getHoraSalida())
                    .put("horaLlegada", ruta.getHoraLlegada()));
        }

        for(Vuelo vuelo : aerolinea.getVuelos()) {
            jVuelos.put(new JSONObject().put("codigoRuta", vuelo.getRuta().getCodigoRuta()).put("fecha", vuelo.getFecha())
                    .put("avion", vuelo.getAvion().getNombre()));
        }

        raiz.put("aeropuertos", jAeropuertos);
        raiz.put("aviones", jAviones);
        raiz.put("rutas", jRutas);
        raiz.put("vuelos", jVuelos);

        try(PrintWriter escritor = new PrintWriter(archivo)) {
            raiz.write(escritor, 2, 0);
        }
    }

    private void agregarAeropuerto(Aeropuerto aeropuerto, Set<String> codigos, JSONArray jAeropuertos) {
        if(codigos.add(aeropuerto.getCodigo())) {
            jAeropuertos.put(new JSONObject().put("nombre", aeropuerto.getNombre()).put("codigo", aeropuerto.getCodigo())
                    .put("nombreCiudad", aeropuerto.getNombreCiudad()).put("latitud", aeropuerto.getLatitud())
                    .put("longitud", aeropuerto.getLongitud()));
        }
    }
}
