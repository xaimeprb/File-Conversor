/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.proyectojaime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menú en el que se le pedirá al usuario a qué tipo de archivo quiere pasar el suyo
 * @author Diurno
 * @since 08/10/2025
 */
public class ProyectoJaime {

    public static void main(String[] args) {
        
       Scanner sc = new Scanner(System.in);
       
       String extension = null;
       
       String extensionTxt = "txt";
       String extensionDat = "dat";
       String extensionPro = "properties";
       String extensionXml = "xml";
       
       EsquemaRegistro esquemaActual = null;   // si el .dat tiene esquema conocido, lo guardamos aquí
       String rutaEsquema = null;

       
       String rutaUsuario = null;
       
       boolean archivoValido = false;
              
       while (!archivoValido) {
           
           System.out.println("Introduzca la ruta relativa del archivo: ");
           rutaUsuario = sc.nextLine();
           
           // Comprobaciones
           String[] partesRuta = rutaUsuario.trim().split("\\.");
           
           extension = partesRuta[partesRuta.length - 1].toLowerCase();

           if (partesRuta.length < 2) {
               
               System.out.println("El archivo introducido no tiene extensión");
               continue;
               
           }
           
           if (!extension.equals(extensionTxt) && !extension.equals(extensionDat) && !extension.equals(extensionPro) && !extension.equals(extensionXml)){
               
               System.out.println("El archivo introducido es inválido: ." + extension);
               continue;
               
           }
           
           // txt / properties / xml: marcado como válido directamente
           if (extension.equals(extensionTxt) || extension.equals(extensionPro) || extension.equals(extensionXml)) {
               
               System.out.println("El archivo " + "." + extension + " introducido es válido");
               archivoValido = true;
               continue;
               
           }
           
           // dat: preguntar por el esquema
           if (extension.equals(extensionDat)) {
               
               System.out.println("El archivo " + "." + extension + " introducido es válido");
               
               System.out.println("La estructura del archivo es conocida? (S/n)");
               String respuesta = sc.nextLine().trim();
               
               if (respuesta.equalsIgnoreCase("s")) {
                   
                   // Mandamos información con la estructura dentro de un txt
                   System.out.println("Introduce la ruta relativa del archivo de ESQUEMA ");
                   rutaEsquema = sc.nextLine();
                   
                   // Ejemplo del archivo: alumno (nombre de entrada)
                   // Id int 4 (nombre campo | tipo dato | tamaño)
                   // Nombre String 20
                   
                   // Parseamos y guardamos el esquema para usarlo luego
                   esquemaActual = esquemaRegistroParsear(rutaEsquema);
                   archivoValido = true;
                   
               } else { // Si no sabemos la estructura, no se hace nada y pedimos otra ruta
                   
                   continue;
                   
               }
               
           }
           
       }
       
       int option;

        do {

            System.out.println("======== MENÚ ========");
            System.out.println("Elija a qué tipo de fichero quiere pasarlo");
            System.out.println("1.- TXT");
            System.out.println("2.- DAT");
            System.out.println("3.- PROPERTIES");
            System.out.println("4.- XML");
            System.out.println("0.- Salir del programa");

            option = sc.nextInt();
            sc.nextLine();

            switch (option) {

                case 1: //TXT

                    if (extension == null) {

                        System.out.println("La extension es nula, revisa el código");

                    }

                    if (extension.equalsIgnoreCase(extensionTxt)) {

                        System.out.println("Convirtiendo en txt...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarTxt2Txt(rutaUsuario, ficheroNuevo);

                    }

                    if (extension.equalsIgnoreCase(extensionDat)) { // "Pasamos el dat a txt sin problema pidiendo el nuevo nombre del archivo"

                        System.out.println("Convirtiendo en txt...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        // Si no conocemos la estructura no se hace nada (según el enunciado)
                        if (esquemaActual == null) {
                            System.out.println("No se conoce la estructura del .dat. No se realizará la conversión a TXT.");
                            break;
                        }

                        FileConversor.transformarDat2Txt(rutaUsuario, ficheroNuevo, esquemaActual);

                    }

                    if (extension.equalsIgnoreCase(extensionXml)) { // Se leerá mediante SAX y se irá escribiendo en un TXT especificando si es elemento o valor

                        System.out.println("Convirtiendo en txt...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarXml2Txt(rutaUsuario, ficheroNuevo);

                    }

                    break;

                case 2: // DAT

                    if (extension == null) {

                        System.out.println("La extension es nula, revisa el código");

                    }

                    if (extension.equalsIgnoreCase(extensionTxt)) {

                        System.out.println("Convirtiendo en dat...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarTxt2Dat(rutaUsuario, ficheroNuevo);

                    }

                    if (extension.equalsIgnoreCase(extensionDat)) { // Si la extensión es .dat tendremos estas diferentes opciones

                         int opcionCase2 = 0;

                         while (opcionCase2 != 0 ) {

                             System.out.println("Introduce una opción DAT: ");
                             System.out.println("1.- BORRAR");
                             System.out.println("2.- MODIFICAR");
                             System.out.println("3.- AÑADIR");
                             System.out.println("4.- LEER");
                             System.out.println("0.- Volver");

                             opcionCase2 = sc.nextInt();
                             sc.nextLine();

                             // Cada registro representará un alumno, con estos campos fijos
                             // CAMPO    ||     TIPO     ||      BYTES       ||      DESCRIPCIÓN
                             //  id             int                4                 Identificador
                             // nombre          String (20 char)   20 bytes          Texto fijo, relleno con espacios
                             // activo          boolean            1                 Borrado lógico

                             switch (opcionCase2) {

                                 case 1: // BORRAR: le pasaremos un id y lo borrará lógicamente

                                         System.out.println("Escribe el id para borrarlo (logicamente)");
                                         int idBorrar = sc.nextInt();

                                         sc.nextLine();

                                         boolean borrado = borrarLogicamenteRegistro(rutaUsuario, idBorrar);

                                         if (borrado) {

                                             System.out.println("Registro con id = " + idBorrar + " marcado como borrado");

                                         } else {

                                             System.out.println("No se encontró el registro con id = " + idBorrar);

                                         }

                                     break;

                                 case 2: // MODIFICAR: le pasaremos un id, el nombre del campo que se quiere modificar y el nuevo valor. Ejemplo: 5 Nombre Pepe

                                         System.out.println("Introduce el id del alumno a modificar:");
                                         int idModificar = sc.nextInt();

                                         sc.nextLine();

                                         System.out.println("Introduce el nombre del campo a modificar (Id | Nombre | Activo):");
                                         String campo = sc.nextLine().trim();

                                         System.out.println("Introduce el nuevo valor:");
                                         String nuevoValor = sc.nextLine();

                                         boolean modificado = modificarCampoRegistro(rutaUsuario, idModificar, campo, nuevoValor);

                                         if (modificado) {

                                             System.out.println("Registro modificado correctamente.");

                                         } else {

                                             System.out.println("No se pudo modificar (id no encontrado o campo inválido)");

                                         }

                                     break;

                                 case 3: // AÑADIR: le pasaremos en orden los campos a introducir. Ejemplo: 7 Pepa

                                         System.out.println("Introduce el id:");
                                         int idNuevo = sc.nextInt();
                                         sc.nextLine();

                                         System.out.println("Introduce el nombre (máx 20 caracteres, se truncará si es más largo):");
                                         String nombreNuevo = sc.nextLine();

                                         System.out.println("¿Activo? (S/N):");
                                         String respActivo = sc.nextLine().trim();
                                         boolean activoNuevo = respActivo.equalsIgnoreCase("s");

                                         boolean anadido = anadirRegistro(rutaUsuario, idNuevo, nombreNuevo, activoNuevo);

                                         if (anadido) {

                                             System.out.println("Registro añadido correctamente.");

                                         } else {

                                             System.out.println("No se pudo añadir el registro.");

                                         }

                                     break;

                                 case 4: // LEER: se pasa un id y devuelve la información de ese alumno

                                         System.out.println("Introduce el id a leer:");
                                         int idLeer = sc.nextInt();

                                         sc.nextLine();

                                         String info = leerRegistroPorId(rutaUsuario, idLeer);

                                         if (info != null) {

                                             System.out.println(info);

                                         } else {

                                             System.out.println("No se encontró el registro con id=" + idLeer);

                                         }

                                     break;


                                 case 0:

                                     // volver al menú principal
                                     break;

                                 default:

                                     System.out.println("La opción introducida es inválida");
                                     throw new AssertionError();

                             }

                         }

                    }

                    if (extension.equalsIgnoreCase(extensionXml)) { // Se leerá del XML como objetos y se escribirá en un .dat como objetos

                        System.out.println("Convirtiendo en dat...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarXml2Dat(rutaUsuario, ficheroNuevo);

                    }

                    break;

                case 3: // PROPERTIES

                    if (extension == null) {

                        System.out.println("La extension es nula, revisa el código");

                    }

                    if (extension.equalsIgnoreCase(extensionTxt)) {

                        System.out.println("Convirtiendo en properties...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarTxt2Properties(rutaUsuario, ficheroNuevo);

                    }

                    if (extension.equalsIgnoreCase(extensionDat)) { // Creamos tanto properties como registros haya con el nombre del campo: valor del campo

                        System.out.println("Convirtiendo en properties...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarDat2Properties(rutaUsuario, ficheroNuevo);

                    }

                    if (extension.equalsIgnoreCase(extensionXml)) { //  Solamente se pasarán las etiquetas que tengan valor. 
                        // Se creará un fichero de configuración por cada elemento, en este caso, alumno. Por ejemplo, el fichero alumno1.properties tendría: id = 1 || nombre = "Laura"

                        System.out.println("Convirtiendo en properties...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarXml2Properties(rutaUsuario, ficheroNuevo);

                    }

                    break;

                case 4: // XML

                    if (extension == null) {

                        System.out.println("La extension es nula, revisa el código");

                    }

                    if (extension.equalsIgnoreCase(extensionTxt)) {

                        System.out.println("No puede convertir un archivo TXT a XML.");

                    }

                    if (extension.equalsIgnoreCase(extensionDat)) { // Se puede escribir con DOM o con objetos XStream para probar las dos formas

                        System.out.println("Convirtiendo en xml...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        // TODO: hacer dos opciones DOM y XStream

                        FileConversor.transformarDat2Xml(rutaUsuario, ficheroNuevo);

                    }

                    if (extension.equalsIgnoreCase(extensionXml)) { // Se leerá el XML original mediante DOM y se irá copiando el código en otro XML como si fueran objetos

                        System.out.println("Convirtiendo en xml...");
                        System.out.println("Introduce el nombre del fichero nuevo");
                        String ficheroNuevo = sc.nextLine();

                        FileConversor.transformarXml2Xml(rutaUsuario, ficheroNuevo);

                    }

                    break;

                case 0: // Salir

                    System.out.println("Saliendo del programa...");

                    break;
                default:

                    System.out.println("La opción introducida es inválida");
                    throw new AssertionError();

            }

        } while (option != 0);

       
        sc.close();
        
    }

    /**
     * Lee un fichero de esquema y contruye un Esquema Registro, asumiento que el formato es siempre correcto, sin realizar validaciones
     * Primera línea: Nombre de la entidad
     * Siguientes: Nombre campo / Tipo: int | String / Tamaño: 4 para int; > 0 para String
     * @param rutaArchivo nombre del archivo introducido
     */
    public static EsquemaRegistro esquemaRegistroParsear(String rutaArchivo) {
        
        String nombreEntidad = null;
        List<CampoDefinicion> campos = null;
        
        try {
            
            File ficheroEsquema = new File(rutaArchivo);
            
            // Leemos todas las líneas (UTF-8)
            List<String> lineas = Files.readAllLines(ficheroEsquema.toPath(), StandardCharsets.UTF_8);
            
            // Limpiamos de la lista las líneas vacías
            List<String> lineasUtiles = new ArrayList<>();
            
            for(String linea : lineas) {
                
                if (linea == null) {continue;}
                
                String t = linea.trim();
                
                if (t.isEmpty()){continue;}
                
                lineasUtiles.add(t);
                
            }
            
            // Primera línea: nombre de la entidad
            nombreEntidad = lineasUtiles.get(0);
            
            // Resto de las líneas
            campos = new ArrayList<>();
            
            for(int i = 1; i < lineasUtiles.size(); i++) {
                
                String[] partes = lineasUtiles.get(i).split("\\s+"); // Espacios (uno o más)
                
                String nombreCampo = partes[0];
                String tipoTexto = partes[1].toLowerCase();
                int longitud = Integer.parseInt(partes[2]);
                
                TipoCampo tipoCampo;
                
                if ("int".equals(tipoTexto)) {
                    
                    tipoCampo = TipoCampo.ENTERO;
                    
                } else {
                    
                    tipoCampo = TipoCampo.CADENA;
                    
                }
                
                campos.add(new CampoDefinicion(nombreCampo, tipoCampo, longitud));
                
            }
            
            return new EsquemaRegistro(nombreEntidad, campos);
            
        } catch (IOException ex) {
            
            System.getLogger(ProyectoJaime.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.err.println("Error en la entrada o salida del fichero");
            return null;
            
        }
        
    }

    private static final int LONG_NOMBRE = 20;            // bytes fijos para el nombre
    private static final int TAM_REGISTRO = 4 + 20 + 1;   // id(4) + nombre(20) + activo(1)

    /**
     * Busca la posición en bytes del registro cuyo id coincide
     * Si no existe, devuelve -1.
     * @param rutaDat
     * @param idBuscado
     * @return 
     */
    private static long buscarPosicionRegistroPorId(String rutaDat, int idBuscado) {
        
        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "r")) {

            long pos = 0;
            while (pos < raf.length()) {
                
                raf.seek(pos);

                int id = raf.readInt();

                // leer nombre para avanzar el puntero
                byte[] buf = new byte[LONG_NOMBRE];
                raf.readFully(buf);

                boolean activo = raf.readBoolean();

                if (id == idBuscado) {
                    
                    return pos; // posición del inicio del registro
                
                }

                pos += TAM_REGISTRO;
            }

        } catch (IOException e) {
            
            System.getLogger(ProyectoJaime.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error buscando el registro: " + e.getMessage());
            
        }
        
        return -1;
        
    }

    /**
     * Escribe una cadena en tamaño fijo (LONG_NOMBRE) en UTF-8, truncando o rellenando con espacios
     * @param raf
     * @param nombre
     * @throws IOException 
     */
    private static void escribirNombreFijo(RandomAccessFile raf, String nombre) throws IOException {
        
        byte[] destino = new byte[LONG_NOMBRE];
        
        // Convertimos el nombre a bytes en UTF-8. Si es null, usamos un array vacío.
        byte[] origen;
        
        if (nombre != null) {
            
            origen = nombre.getBytes(StandardCharsets.UTF_8);
            
        } else {
            
            origen = new byte[0];
            
        }


        int len = Math.min(destino.length, origen.length);
        System.arraycopy(origen, 0, destino, 0, len);

        // Rellenar con espacios el resto
        for (int i = len; i < destino.length; i++) {
            
            destino[i] = 32;
            
        }

        raf.write(destino);
    }

    /**
     * Lee una cadena de tamaño fijo LONG_NOMBRE desde la posición actual del RAF.
     */
    private static String leerNombreFijo(RandomAccessFile raf) throws IOException {
        byte[] buf = new byte[LONG_NOMBRE];
        raf.readFully(buf);

        int fin = buf.length;
        while (fin > 0 && (buf[fin - 1] == 0 || buf[fin - 1] == 32)) fin--;

        return new String(buf, 0, fin, StandardCharsets.UTF_8).trim();
    }

    /**
     * Marca como borrado lógico (activo=false) el registro con el id indicado.
     */
    private static boolean borrarLogicamenteRegistro(String rutaDat, int id) {
        
        long pos = buscarPosicionRegistroPorId(rutaDat, id);
        
        if (pos < 0){
        
            return false;
        
        } 

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "rw")) {
            
            // activo está al final del registro: offset = pos + 4 + 20
            long posFlag = pos + 4 + LONG_NOMBRE;
            raf.seek(posFlag);
            raf.writeBoolean(false);
            
            return true;
            
        } catch (IOException e) {
            
            System.getLogger(ProyectoJaime.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al borrar lógicamente: " + e.getMessage());
            
            return false;
        }
    }

    /**
     * Modifica un campo concreto del registro con el id indicado.
     * Campo puede ser: "Id", "Nombre", "Activo" (no sensible a mayúsculas).
     */
    private static boolean modificarCampoRegistro(String rutaDat, int id, String campo, String nuevoValor) {
        
        long pos = buscarPosicionRegistroPorId(rutaDat, id);
        
        if (pos < 0){
        
            return false;
        
        }

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "rw")) {

            if (campo.equalsIgnoreCase("Id")) {
                
                raf.seek(pos);
                int idNuevo = Integer.parseInt(nuevoValor);
                raf.writeInt(idNuevo);
                
                return true;
                
            }

            if (campo.equalsIgnoreCase("Nombre")) {
                
                raf.seek(pos + 4);
                escribirNombreFijo(raf, nuevoValor);
                
                return true;
                
            }

            if (campo.equalsIgnoreCase("Activo")) {
                
                boolean activoNuevo = nuevoValor.equalsIgnoreCase("true") || nuevoValor.equalsIgnoreCase("s");
                raf.seek(pos + 4 + LONG_NOMBRE);
                raf.writeBoolean(activoNuevo);
                
                return true;
                
            }

        } catch (IOException e) {
            
            System.getLogger(ProyectoJaime.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al modificar el registro: " + e.getMessage());
            
        } catch (NumberFormatException nfe) {
            
            System.err.println("Valor no numérico para Id: " + nuevoValor);
            
        }

        return false;
    }

    /**
     * Añade un registro al final del fichero.
     */
    private static boolean anadirRegistro(String rutaDat, int id, String nombre, boolean activo) {
        
        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "rw")) {
            
            raf.seek(raf.length()); // al final del archivo

            raf.writeInt(id);
            escribirNombreFijo(raf, nombre);
            raf.writeBoolean(activo);

            return true;
            
        } catch (IOException e) {
            
            System.getLogger(ProyectoJaime.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al añadir registro: " + e.getMessage());
            
            return false;
            
        }
    }

    /**
     * Devuelve un String con la información del registro (o null si no existe).
     */
    private static String leerRegistroPorId(String rutaDat, int idBuscado) {
        
        long pos = buscarPosicionRegistroPorId(rutaDat, idBuscado);
        if (pos < 0) return null;

        try (RandomAccessFile raf = new RandomAccessFile(rutaDat, "r")) {
            
            raf.seek(pos);

            int id = raf.readInt();
            String nombre = leerNombreFijo(raf);
            boolean activo = raf.readBoolean();

            String estado = activo ? "ACTIVO" : "BORRADO";
            return "id=" + id + "\n" +
                   "nombre=" + nombre + "\n" +
                   "estado=" + estado;

        } catch (IOException e) {
            
            System.getLogger(ProyectoJaime.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al leer registro: " + e.getMessage());
            
            return null;
            
        }
    }
}
