package com.mycompany.proyectojaime.app;

import com.mycompany.proyectojaime.conversor.FileConversor;
import com.mycompany.proyectojaime.dat.CampoDefinicion;
import com.mycompany.proyectojaime.dat.EsquemaRegistro;
import com.mycompany.proyectojaime.dat.DATController;

import java.util.Scanner;

/**
 * Menú principal que solicita al usuario un archivo y permite convertirlo
 * a distintos formatos según el enunciado
 * @author Jaime Pérez Roget Blanco
 * @since 17/11/2025
 */
public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        String extension = null;
        String extensionTxt = "txt";
        String extensionDat = "dat";
        String extensionPro = "properties";
        String extensionXml = "xml";

        EsquemaRegistro esquemaActual = null;
        String rutaUsuario = null;

        boolean archivoValido = false;

        while (!archivoValido) {

            System.out.println("Introduzca la ruta relativa del archivo: ");
            rutaUsuario = sc.nextLine();

            String[] partesRuta = rutaUsuario.trim().split("\\.");

            if (partesRuta.length < 2) {

                System.out.println("El archivo introducido no tiene extensión");

                continue;

            }

            extension = partesRuta[partesRuta.length - 1].toLowerCase();

            if (!extension.equals(extensionTxt) && !extension.equals(extensionDat) && !extension.equals(extensionPro) && !extension.equals(extensionXml)) {

                System.out.println("El archivo introducido es inválido: ." + extension);

                continue;

            }

            if (extension.equals(extensionTxt) || extension.equals(extensionPro) || extension.equals(extensionXml)) {

                System.out.println("El archivo ." + extension + " introducido es válido");

                archivoValido = true;

                continue;

            }

            if (extension.equals(extensionDat)) {

                System.out.println("El archivo ." + extension + " introducido es válido");

                System.out.println("¿La estructura del archivo es conocida? (S/n)");
                String respuesta = sc.nextLine().trim();

                if (respuesta.equalsIgnoreCase("s")) {

                    System.out.println("Introduce la ruta relativa del archivo de ESQUEMA: ");
                    String rutaEsquema = sc.nextLine();

                    esquemaActual = new DATController().parsearEsquema(rutaEsquema);

                    archivoValido = true;

                }
            }
        }

        int option;

        do {

            System.out.println("======== MENÚ ========");
            System.out.println("1.- TXT");
            System.out.println("2.- DAT");
            System.out.println("3.- PROPERTIES");
            System.out.println("4.- XML");
            System.out.println("0.- Salir del programa");

            option = sc.nextInt();
            sc.nextLine();

            switch (option) {

                case 1: // TXT

                    System.out.println("Introduce el nombre del fichero nuevo:");
                    String nuevoTxt = sc.nextLine();

                    if (extension.equals(extensionTxt)) {

                        new FileConversor().transformarTxt2Txt(rutaUsuario, nuevoTxt);

                    } else if (extension.equals(extensionDat)) {

                        if (esquemaActual == null) {

                            System.out.println("No se conoce la estructura del .dat, no se puede convertir");

                        } else {

                            new FileConversor().transformarDat2Txt(rutaUsuario, nuevoTxt, esquemaActual);

                        }

                    } else if (extension.equals(extensionXml)) {

                        new FileConversor().transformarXml2Txt(rutaUsuario, nuevoTxt);

                    }

                    break;

                case 2: // DAT

                    if (extension.equals(extensionTxt)) {

                        System.out.println("Introduce el nombre del fichero nuevo:");
                        String nuevoDat = sc.nextLine();

                        new FileConversor().transformarTxt2Dat(rutaUsuario, nuevoDat);
                    }

                    if (extension.equals(extensionDat)) {

                        int opDat = -1;

                        while (opDat != 0) {

                            System.out.println("Opciones DAT:");
                            System.out.println("1.- BORRAR");
                            System.out.println("2.- MODIFICAR");
                            System.out.println("3.- AÑADIR");
                            System.out.println("4.- LEER");
                            System.out.println("0.- Volver");

                            opDat = sc.nextInt();
                            sc.nextLine();

                            switch (opDat) {

                                case 1:

                                    System.out.println("ID a borrar:");
                                    int idBorrar = sc.nextInt(); sc.nextLine();

                                    boolean borrado = DATController.borrarLogicamenteRegistro(rutaUsuario, idBorrar);

                                    if (borrado) {

                                        System.out.println("Registro borrado.");

                                    } else {

                                        System.out.println("No encontrado.");

                                    }

                                    break;

                                case 2:

                                    System.out.println("ID a modificar:");
                                    int idModificar = sc.nextInt(); sc.nextLine();

                                    System.out.println("Campo (Id | Nombre | Activo):");
                                    String campo = sc.nextLine();

                                    System.out.println("Nuevo valor:");
                                    String nuevoValor = sc.nextLine();

                                    boolean mod = DATController.modificarCampo(rutaUsuario, idModificar, campo, nuevoValor);

                                    if (mod) {

                                        System.out.println("Registro modificado");

                                    } else {

                                        System.out.println("No se pudo modificar.");

                                    }

                                    break;

                                case 3:
                                    System.out.println("ID nuevo:");
                                    int idNuevo = sc.nextInt(); sc.nextLine();
                                    System.out.println("Nombre:");
                                    String nombreNuevo = sc.nextLine();
                                    System.out.println("¿Activo? (S/N):");
                                    boolean activoNuevo = sc.nextLine().trim().equalsIgnoreCase("s");

                                    boolean añadido = DATController.anadirRegistro(rutaUsuario, idNuevo, nombreNuevo, activoNuevo);

                                    if (añadido) {

                                        System.out.println("Registro añadido");

                                    } else {

                                        System.out.println("No se pudo añadir");

                                    }
                                    break;

                                case 4:

                                    System.out.println("ID a leer:");
                                    int idLeer = sc.nextInt(); sc.nextLine();

                                    String info = DATController.leerRegistroPorId(rutaUsuario, idLeer);

                                    if (info != null) {

                                        System.out.println(info);

                                    } else {

                                        System.out.println("No encontrado.");

                                    }

                                    break;
                                case 0:

                                    System.out.println("Volviendo...");

                                    break;

                            }
                        }
                    }

                    if (extension.equals(extensionXml)) {

                        System.out.println("Introduce el nombre del fichero nuevo:");
                        String nuevoDatFromXml = sc.nextLine();

                        new FileConversor().transformarXml2Dat(rutaUsuario, nuevoDatFromXml);

                    }

                    break;

                case 3: // PROPERTIES

                    System.out.println("Introduce nombre del fichero nuevo:");
                    String nuevoProps = sc.nextLine();

                    if (extension.equals(extensionTxt)){

                        new FileConversor().transformarTxt2Properties(rutaUsuario, nuevoProps);

                    }

                    if (extension.equals(extensionDat)){

                        new FileConversor().transformarDat2Properties(rutaUsuario, nuevoProps);

                    }

                    if (extension.equals(extensionXml)){

                        new FileConversor().transformarXml2Properties(rutaUsuario, nuevoProps);

                    }

                    break;

                case 4: // XML

                    if (extension.equals(extensionTxt)) {

                        System.out.println("No se puede convertir TXT a XML.");

                    }

                    System.out.println("Introduce nombre del fichero nuevo:");
                    String nuevoXml = sc.nextLine();

                    if (extension.equals(extensionDat)) {

                        new FileConversor().transformarDat2Xml(rutaUsuario, nuevoXml);

                    }

                    if (extension.equals(extensionXml)) {

                        new FileConversor().transformarXml2Xml(rutaUsuario, nuevoXml);

                    }

                    break;

                case 0:

                    System.out.println("Saliendo...");

                    break;
            }

        } while (option != 0);

        sc.close();
    }
}
