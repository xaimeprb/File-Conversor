/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectojaime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Clase conversora de archivos con diferentes extensiones: 1)txt 2)dat 3)properties 4)xml
 * @author Jaime
 * @since 08/10/2025
 */
public class FileConversor {

    /**
     * Se copiará el archivo recibido y se pegará al fichero nuevo 
     * @param rutaUsuario ruta relativa del archivo recibido
     * @param ficheroNuevo nombre del fichero nuevo (sin extensión)
     */
    static void transformarTxt2Txt(String rutaUsuario, String ficheroNuevo) {
        
        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".txt"); // Archivo destino
                
        try (BufferedReader br = new BufferedReader(new FileReader(file)); BufferedWriter bw = new BufferedWriter(new FileWriter(newFile))) {
            
            String linea;
            
            while ((linea = br.readLine()) != null) {
                
                bw.write(linea);
                bw.newLine();
                
            }
            
        } catch (IOException ex) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.err.println("Se ha producido un error de E/S");
            
        }
        
        System.out.println("Archivo copiado correctamente como: " + newFile.getName());
        
    }

    /**
     * Método que copia a un fichero binario sin saber tipos, sólo byte a byte y pega en otro con extensión .dat
     * @param rutaUsuario ruta relativa del archivo recibido
     * @param ficheroNuevo nombre del fichero nuevo
     */
    static void transformarTxt2Dat(String rutaUsuario, String ficheroNuevo) {
        
        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".dat");
        
        try (FileInputStream fis = new FileInputStream(file); FileOutputStream fos = new FileOutputStream(newFile)) {
            
            // Podemos hacerlo de dos maneras, byte a byte como se pide o por bloque
            
            int byteLeido;
            
            while ((byteLeido = fis.read()) != -1 ) {
                
                fos.write(byteLeido);
                
            }
            
        } catch (FileNotFoundException ex) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.err.println("No existe el archivo o por alguna razón es inaccesible");
            
        } catch (IOException ex) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.err.println("Se ha producido un error de E/S");
            
        }
        
        System.out.println("Archivo copiado correctamente como: " + newFile.getName());
        
    }

    /**
     * Método que comprueba que el fichero .txt tenga la estructura "clave : valor" en todo momento para poder pasarlo,
     * si no, se dirá que no se puede y lo pasa a un archivo con extensión properties
     * @param rutaUsuario ruta relativa del archivo recibido
     * @param ficheroNuevo nombre del fichero nuevo
     */
    static void transformarTxt2Properties(String rutaUsuario, String ficheroNuevo) {
        
        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".properties");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file));BufferedWriter bw = new BufferedWriter(new FileWriter(newFile))) {
            
            String linea;

            while ((linea = br.readLine()) != null) {
                
                linea = linea.trim(); // Limpiamos espacios
                
                if (linea.isEmpty()) { // Permite líneas vacías opcionalmente
                    
                    continue;
                    
                }
                
                if (!linea.contains("=")) {
                    
                    System.out.println("El archivo .txt no tiene formato clave = valor. No ha sido convertido.");
                    break;
                    
                }
                
                String[] partes = linea.split("=", 2);
                
                if (partes.length < 2 || partes[0].isBlank() || partes[1].isBlank()) {
                    
                    System.out.println("El archivo .txt no tiene formato clave = valor. No ha sido convertido.");
                    break;
                    
                }
                
                // Si lo anterior se cumple se escribirá en el nuevo fichero tal cual
                bw.write(linea);
                bw.newLine();
                
            }
            
        } catch (FileNotFoundException ex) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.err.println("No existe el archivo o por alguna razón es inaccesible");
            
        } catch (IOException ex) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.err.println("Se ha producido un error de E/S");
            
        }
        
        System.out.println("Archivo copiado correctamente como: " + newFile.getName());
        
    }

    /**
     * Método que lee un archivo DAT y escribe en TXT usando el esquema para leer cada campo en binario y volcar como clave=valor
     * @param rutaUsuario
     * @param ficheroNuevo
     * @param esquema 
     */
    static void transformarDat2Txt(String rutaUsuario, String ficheroNuevo, EsquemaRegistro esquema) {
       
        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".txt");

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             BufferedWriter bw = new BufferedWriter(new FileWriter(newFile, StandardCharsets.UTF_8))) {

            // Recorremos todo el fichero registro a registro
            while (raf.getFilePointer() < raf.length()) {

                // Para cada registro, leemos los campos en el orden del esquema
                for (CampoDefinicion c : esquema.getCampos()) {

                    String clave = c.getNombre();
                    String valor;

                    switch (c.getTipoCampo()) {

                        case ENTERO -> {

                            int n = raf.readInt(); // 4 bytes
                            valor = Integer.toString(n);

                        }
                        case CADENA -> {

                            int len = c.getLongitud(); // longitud fija en bytes
                            byte[] buf = new byte[len];
                            raf.readFully(buf); // leer exactamente en bytes

                            // quitar padding nulos/espacios al final y decodificar
                            int fin = buf.length;
                            while (fin > 0 && (buf[fin - 1] == 0 || buf[fin - 1] == 32)) fin--;
                            valor = new String(buf, 0, fin, StandardCharsets.UTF_8).trim();
                        }
                        default -> throw new IOException("Tipo no soportado en esquema.");
                    }

                    bw.write(clave + "=" + valor);
                    bw.newLine();
                }

            }

            System.out.println("DAT volcado correctamente a: " + newFile.getName());

        } catch (IOException e) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al convertir DAT a TXT: " + e.getMessage());
            
        }
    }

    /**
     * Método que transforma un archivo XML a TXT mediante DOM Parser
     * @param rutaUsuario ruta relativa del archivo recibido
     * @param ficheroNuevo nombre del fichero nuevo 
     */
    static void transformarXml2Txt(String rutaUsuario, String ficheroNuevo) {
        
        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".txt");
        
        try {
            
            // Primero cargamos y parseamos el archivo XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document documento = builder.parse(rutaUsuario);
            
            // Normalizamos el documento
            documento.getDocumentElement().normalize();
            
            // Obtenemos primero los nodos, en este caso es "persona"
            NodeList listaObjeto = documento.getElementsByTagName("persona");
            
            // Crear archivo TXT
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
            
            for(int i = 0; i < listaObjeto.getLength(); i++) {
                
                Node nodo = listaObjeto.item(i);
                
                if(nodo.getNodeType() == Node.ELEMENT_NODE) {
                    
                    Element elemento = (Element) nodo;
                    String nombre = elemento.getElementsByTagName("nombre").item(0).getTextContent();
                    String edad = elemento.getElementsByTagName("edad").item(0).getTextContent();
                    
                    bw.write(nombre + " - " + edad + " años");
                    bw.newLine();
                    
                }
                
            }
            
            bw.close();
            System.out.println("Conversión de XML a TXT completada");
            
        } catch (ParserConfigurationException ex) {
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (SAXException ex) {
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }

    /**
     * Método en el cual se leerá del XML como objetos y se escribirá en el DAT como objetos
     * @param rutaUsuario ruta relativa del archivo recibido
     * @param ficheroNuevo nombre del fichero nuevo 
     */
    static void transformarXml2Dat(String rutaUsuario, String ficheroNuevo) {
        
        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".dat");
        
        try {
            
            //Parseamos XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document documento = builder.parse(file);
            documento.getDocumentElement().normalize();
            
            // Recorremos los nodos
            NodeList nodos = documento.getElementsByTagName("");
            
        } catch (SAXException ex) {
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (ParserConfigurationException ex) {
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }

    /**
    * Crea un .properties por cada registro del .dat.
    * Layout asumido por registro: id (int 4) + nombre (cadena fija 20 bytes, padding con 0/espacios).
    * El nombre de salida será: <ficheroNuevo><id>.properties en la misma carpeta del origen.
    * @param rutaUsuario ruta relativa del archivo .dat
    * @param ficheroNuevo prefijo del fichero nuevo (sin extensión)
    */
   static void transformarDat2Properties(String rutaUsuario, String ficheroNuevo) {

       File origen = new File(rutaUsuario);
       File carpetaSalida = origen.getParentFile() != null ? origen.getParentFile() : new File(".");
       final int LONG_NOMBRE = 20;

       try (RandomAccessFile raf = new RandomAccessFile(origen, "r")) {

           while (raf.getFilePointer() < raf.length()) {
               int id = raf.readInt(); // 4 bytes

               byte[] bufNombre = new byte[LONG_NOMBRE];
               raf.readFully(bufNombre);
               int fin = bufNombre.length;
               while (fin > 0 && (bufNombre[fin - 1] == 0 || bufNombre[fin - 1] == 32)) fin--;
               String nombre = new String(bufNombre, 0, fin, StandardCharsets.UTF_8).trim();

               // Crear properties por registro
               java.util.Properties p = new java.util.Properties();
               p.setProperty("id", Integer.toString(id));
               p.setProperty("nombre", nombre);

               File out = new File(carpetaSalida, ficheroNuevo + id + ".properties");
               try (FileOutputStream fos = new FileOutputStream(out)) {
                   p.store(fos, "Registro exportado desde DAT");
               }
               System.out.println("Generado: " + out.getName());
           }

       } catch (IOException e) {
           System.getLogger(FileConversor.class.getName())
                 .log(System.Logger.Level.ERROR, (String) null, e);
           System.err.println("Error al convertir DAT a PROPERTIES: " + e.getMessage());
       }
   }

    /**
    * Convierte un XML a varios .properties (uno por cada elemento-hijo del root).
    * Solo guarda etiquetas con texto no vacío.
    * Ejemplo de salida: <ficheroNuevo>1.properties, <ficheroNuevo>2.properties, ...
    *
    * @param rutaUsuario   ruta del XML de origen
    * @param ficheroNuevo  prefijo para los .properties de salida (sin extensión)
    */
    static void transformarXml2Properties(String rutaUsuario, String ficheroNuevo) {

        // Preparar rutas de entrada/salida
        File file = new File(rutaUsuario);
        File newFile = (file.getParentFile() != null)? file.getParentFile(): new File(".");

        try {
            // Cargar y normalizar el XML con DOM
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            Document xml = constructor.parse(file);
            xml.getDocumentElement().normalize();

            // Tomar el elemento raíz y recorrer sus hijos
            Element raiz = xml.getDocumentElement();
            NodeList elementos = raiz.getChildNodes();

            int contador = 1;
            
            for (int i = 0; i < elementos.getLength(); i++) {
                
                Node nodo = elementos.item(i);
                
                // Si el nodo actual no es un ELEMENT_NODE (puede ser texto, espacios, saltos de línea, etc.),
                // no lo procesamos y pasamos al siguiente nodo del listado.
                if (nodo.getNodeType() != Node.ELEMENT_NODE) {
                    
                    continue;
                    
                }

                Element elemento = (Element) nodo;

                // Recolectar pares clave=valor de los hijos (solo elementos con texto)
                Properties props = new Properties();
                NodeList hijos = elemento.getChildNodes();
                
                for (int j = 0; j < hijos.getLength(); j++) {
                    
                    Node hijo = hijos.item(j);
                    
                    // Si el nodo actual no es un ELEMENT_NODE (puede ser texto, espacios, saltos de línea, etc.),
                    // no lo procesamos y pasamos al siguiente nodo del listado.
                    if (nodo.getNodeType() != Node.ELEMENT_NODE) {
                        
                        continue;
                        
                    }

                    String clave = hijo.getNodeName();
                    String valor = hijo.getTextContent().trim();
                    
                    if (!valor.isEmpty()) {
                        
                        props.setProperty(clave, valor);
                        
                    }
                }

                // Si hay datos, guardar un .properties por elemento
                if (!props.isEmpty()) {
                    
                    File salida = new File(newFile, ficheroNuevo + contador + ".properties");
                    
                    try (FileOutputStream fos = new FileOutputStream(salida)) {
                        
                        props.store(fos, "Generado desde XML (solo etiquetas con valor)");
                        
                    }
                    
                    System.out.println("Generado: " + salida.getName());
                    contador++;
                }
            }

        } catch (Exception e) {
            
            System.err.println("Error al convertir XML a PROPERTIES: " + e.getMessage());
            
        }
    }

    /**
    * Convierte un .dat a .xml con DOM.
    * Layout asumido por registro: id (int 4) + nombre (cadena fija 20 bytes).
    * @param rutaUsuario ruta relativa del archivo .dat
    * @param ficheroNuevo nombre del fichero nuevo
    */
    static void transformarDat2Xml(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);
        File destino = new File((origen.getParent() != null ? origen.getParent() + File.separator : "") + ficheroNuevo + ".xml");
        final int LONG_NOMBRE = 20;

        try (RandomAccessFile raf = new RandomAccessFile(origen, "r")) {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element root = doc.createElement("alumnos");
            doc.appendChild(root);

            while (raf.getFilePointer() < raf.length()) {
                
                int id = raf.readInt();

                byte[] buf = new byte[LONG_NOMBRE];
                raf.readFully(buf);
                
                int fin = buf.length;
                
                while (fin > 0 && (buf[fin - 1] == 0 || buf[fin - 1] == 32)) fin--;
                
                String nombre = new String(buf, 0, fin, StandardCharsets.UTF_8).trim();

                Element alumno = doc.createElement("alumno");

                Element eId = doc.createElement("id");
                eId.setTextContent(Integer.toString(id));
                alumno.appendChild(eId);

                Element eNombre = doc.createElement("nombre");
                eNombre.setTextContent(nombre);
                alumno.appendChild(eNombre);

                root.appendChild(alumno);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(destino));

            System.out.println("DAT convertido a XML: " + destino.getName());

        } catch (Exception e) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al convertir DAT a XML: " + e.getMessage());
            
        }
    }


    /**
    * Clona un XML a otro XML (DOM a DOM)
    * @param rutaUsuario ruta relativa del archivo XML origen
    * @param ficheroNuevo nombre del fichero nuevo (sin extensión)
    */
    static void transformarXml2Xml(String rutaUsuario, String ficheroNuevo) {

        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".xml");

        try {
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc),new StreamResult(newFile));

            System.out.println("XML clonado a: " + newFile.getName());

        } catch (Exception e) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al clonar XML: " + e.getMessage());
            
        }
    }

    
}
