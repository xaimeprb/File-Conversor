package com.mycompany.proyectojaime.dat;

import com.mycompany.proyectojaime.conversor.FileConversor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DATController {

    private static final int LONG_NOMBRE = 20;
    private static final int TAM_REGISTRO = 4 + LONG_NOMBRE + 1;

    /**
     * Lee el nombre fijo de longitud LONG_NOMBRE
     */
    private static String leerCadenaFija(RandomAccessFile raf, int longitud) throws IOException {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < longitud; i++) {

            byte b = raf.readByte();

            // Ignoramos padding nulo o espacios
            if (b != 0 && b != 32) {

                sb.append((char) b);

            }
        }

        return sb.toString().trim();
    }

    /**
     * Escribe una cadena con padding hasta LONG_NOMBRE
     */
    private static void escribirCadenaFija(RandomAccessFile raf, String texto, int longitud) throws IOException {

        int i = 0;

        while (i < longitud) {

            if (texto != null && i < texto.length()) {

                raf.writeByte((byte) texto.charAt(i));

            } else {

                raf.writeByte(32); // espacio

            }

            i++;

        }
    }

    /**
     * Busca la posición de un registro por su id
     */
    public static long buscarPosicionRegistro(String rutaDat, int idBuscado) {

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "r")) {

            long offset = 0;

            while (offset < raf.length()) {

                raf.seek(offset);

                int id = raf.readInt();

                for (int i = 0; i < LONG_NOMBRE; i++) {

                    raf.readByte();

                }

                boolean activo = raf.readBoolean();

                if (id == idBuscado) {

                    return offset;

                }

                offset += TAM_REGISTRO;
            }

        } catch (IOException e) {

            System.getLogger(FileConversor.class.getName())
                    .log(System.Logger.Level.ERROR, "Error buscando registro: " + e.getMessage(), e);
        }

        return -1;
    }

    /**
     * Borrado lógico: marca activo=false
     */
    public static boolean borrarLogicamenteRegistro(String rutaDat, int id) {

        long offset = buscarPosicionRegistro(rutaDat, id);

        if (offset < 0) return false;

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "rw")) {

            long offsetFlag = offset + 4 + LONG_NOMBRE;
            raf.seek(offsetFlag);
            raf.writeBoolean(false);

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO, "Registro marcado como borrado correctamente");

            return true;

        } catch (IOException e) {

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, "Error en borrado lógico: " + e.getMessage(), e);

            return false;
        }
    }

    /**
     * Añadir un registro al final.
     */
    public static boolean anadirRegistro(String rutaDat, int id, String nombre, boolean activo) {

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "rw")) {

            raf.seek(raf.length());

            raf.writeInt(id);
            escribirCadenaFija(raf, nombre, LONG_NOMBRE);
            raf.writeBoolean(activo);

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO, "Registro añadido correctamente");

            return true;

        } catch (IOException e) {

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, "Error añadiendo registro: " + e.getMessage(), e);

            return false;

        }
    }

    /**
     * Leer registro por su id
     */
    public static String leerRegistroPorId(String rutaDat, int idBuscado) {

        long offset = buscarPosicionRegistro(rutaDat, idBuscado);

        if (offset < 0) {

            return null;

        }

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "r")) {

            raf.seek(offset);

            int id = raf.readInt();
            String nombre = leerCadenaFija(raf, LONG_NOMBRE);
            boolean activo = raf.readBoolean();

            String estado;

            if (activo) {

                estado = "ACTIVO";

            } else {

                estado = "BORRADO";

            }

            return "id=" + id + "nombre=" + nombre + "estado=" + estado;

        } catch (IOException e) {

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, "Error leyendo registro: " + e.getMessage(), e);

            return null;
        }
    }

    /**
     * Modificar campo (id, nombre, activo).
     */
    public static boolean modificarCampo(String rutaDat, int id, String campo, String nuevoValor) {

        long offset = buscarPosicionRegistro(rutaDat, id);

        if (offset < 0) return false;

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "rw")) {

            switch (campo.toLowerCase()) {

                case "id":

                    raf.seek(offset);
                    raf.writeInt(Integer.parseInt(nuevoValor));

                    break;

                case "nombre":

                    raf.seek(offset + 4);
                    escribirCadenaFija(raf, nuevoValor, LONG_NOMBRE);

                    break;

                case "activo":

                    raf.seek(offset + 4 + LONG_NOMBRE);

                    boolean activoNuevo = nuevoValor.equalsIgnoreCase("true") ||nuevoValor.equalsIgnoreCase("s");

                    raf.writeBoolean(activoNuevo);

                    break;

                default:
                    return false;
            }

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO, "Registro modificado correctamente");

            return true;

        } catch (IOException | NumberFormatException e) {

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, "Error modificando registro: " + e.getMessage(), e);

            return false;
        }
    }


    public EsquemaRegistro parsearEsquema(String rutaEsquema) {

        File archivoEsquema = new File(rutaEsquema);

        if (!archivoEsquema.exists() || !archivoEsquema.isFile()) {

            System.out.println("El archivo de esquema no existe");

            return null;

        }

        try {
            // Leer todas las líneas con el charset por defecto del sistema
            List<String> lineasOriginales = java.nio.file.Files.readAllLines(archivoEsquema.toPath());

            List<String> lineasFiltradas = new ArrayList<>();

            // Quitamos líneas vacías o nulas
            for (String linea : lineasOriginales) {

                if (linea == null) {

                    continue;

                }

                String lineaLimpia = linea.trim();

                if (lineaLimpia.isEmpty()) {

                    continue;

                }

                lineasFiltradas.add(lineaLimpia);

            }

            if (lineasFiltradas.isEmpty()) {

                System.out.println("El archivo de esquema está vacío.");

                return null;

            }

            String nombreEntidad = lineasFiltradas.get(0);

            List<CampoDefinicion> listaCampos = new ArrayList<>();

            for (int i = 1; i < lineasFiltradas.size(); i++) {

                String lineaCampo = lineasFiltradas.get(i);
                String[] partes = lineaCampo.split("\\s+");

                String nombreCampo = partes[0];
                String tipoCampoTexto = partes[1].toLowerCase();
                int longitudCampo = Integer.parseInt(partes[2]);

                TipoCampo tipoCampo;

                if (tipoCampoTexto.equals("int")) {
                    tipoCampo = TipoCampo.ENTERO;
                } else {
                    tipoCampo = TipoCampo.CADENA;
                }

                listaCampos.add(new CampoDefinicion(nombreCampo, tipoCampo, longitudCampo));
            }

            System.out.println("Esquema cargado correctamente: " + nombreEntidad);

            return new EsquemaRegistro(nombreEntidad, listaCampos);

        } catch (Exception e) {

            System.getLogger(DATController.class.getName())
                    .log(System.Logger.Level.ERROR, (String) null, e);

            System.err.println("Error al procesar el archivo de esquema.");
            return null;
        }
    }

}
