package com.mycompany.proyectojaime.conversor;

import java.io.*;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.mycompany.proyectojaime.dat.CampoDefinicion;
import com.mycompany.proyectojaime.dat.EsquemaRegistro;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *  Clase encargada de realizar conversiones entre distintos formatos de fichero: 1)txt 2)dat 3)properties 4)xml
 *
 * @author Jaime Pérez Roget Blanco
 * @since 08/10/2025
 */
public class FileConversor {

    /**
     * Copia un fichero .txt a otro fichero .txt línea a línea
     * @param rutaUsuario ruta relativa del archivo recibido
     * @param ficheroNuevo nombre del fichero nuevo (sin extensión)
     */
    public void transformarTxt2Txt(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);
        File destino = new File(ficheroNuevo + ".txt");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero indicado no existe: " + rutaUsuario);

        }
                
        try (BufferedReader br = new BufferedReader(new FileReader(origen)); BufferedWriter bw = new BufferedWriter(new FileWriter(destino))) {
            
            String linea;
            
            while ((linea = br.readLine()) != null) {
                
                bw.write(linea);
                bw.newLine();
                
            }
            
        } catch (IOException e) {

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO, "Error durante la copia TXT " + e.getMessage());

            throw new RuntimeException("Error durante la conversión TXT a TXT", e);

        }
        
    }

    /**
     * Copia a un fichero binario sin saber tipos, solo byte a byte y pega en otro con extensión .dat
     *
     * @param rutaUsuario ruta relativa del fichero origen
     * @param ficheroNuevo nombre del fichero .dat destino
     */
    public void transformarTxt2Dat(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);
        File destino = new File(ficheroNuevo + ".dat");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero indicado no existe: " + rutaUsuario);

        }
        
        try (FileInputStream fis = new FileInputStream(origen); FileOutputStream fos = new FileOutputStream(destino)) {
            
            int byteLeido;
            
            while ((byteLeido = fis.read()) != -1 ) {
                
                fos.write(byteLeido);
                
            }
            
        } catch (IOException e) {

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, "Error durante la copia TXT a DAT: " + e.getMessage(), e);

        }
        
    }

    /**
     * Convierte un archivo .txt a .properties siempre que todas las líneas tengan la estructura clave=valor,
     * si no, no se genera el fichero .properties
     *
     * @param rutaUsuario ruta del archivo .txt
     * @param ficheroNuevo nombre del fichero .properties
     */
    public void transformarTxt2Properties(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);
        File destino = new File(ficheroNuevo + ".properties");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero .txt no existe: " + rutaUsuario);

        }

        boolean valido = true;

        try (BufferedReader br = new BufferedReader(new FileReader(origen));BufferedWriter bw = new BufferedWriter(new FileWriter(destino))) {
            
            String linea;

            while ((linea = br.readLine()) != null) {
                
                linea = linea.trim();
                
                if (linea.isEmpty()) {
                    
                    continue;
                    
                }
                
                if (!linea.contains("=")) {
                    
                    valido = false;
                    break;
                    
                }
                
                String[] partes = linea.split("=", 2);
                
                if (partes.length < 2 || partes[0].isEmpty() || partes[1].isEmpty()) {
                    
                    valido = false;
                    break;
                    
                }

                bw.write(partes[0].trim() + "=" + partes[1].trim());
                bw.newLine();
                
            }
            
        } catch (IOException e) {

            throw new RuntimeException("Error durante la conversión TXT → properties", e);

        }

        System.Logger logger = System.getLogger(FileConversor.class.getName());

        if (!valido) {

            destino.delete();

            logger.log(System.Logger.Level.ERROR,"Formato no válido: el TXT no cumple clave=valor en todas sus líneas. No se ha convertido.");

            return;
        }

        // Si todo funciona correctamente mandamos un log de éxito
        logger.log(System.Logger.Level.INFO,"Archivo convertido correctamente a properties: " + destino.getAbsolutePath());

    }

    /**
     * Lee un archivo .dat usando un esquema y escribe en un .txt
     * con un formato clave=valor por cada línea
     *
     * @param rutaUsuario ruta del .dat origen
     * @param ficheroNuevo nombre base del .txt
     * @param esquema definición de campos del registro
     */
    public void transformarDat2Txt(String rutaUsuario, String ficheroNuevo, EsquemaRegistro esquema) {

        File origen = new File(rutaUsuario);
        File destino = new File(ficheroNuevo + ".txt");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero indicado no existe: " + rutaUsuario);

        }

        try (RandomAccessFile raf = new RandomAccessFile(origen, "r");
             BufferedWriter bw = new BufferedWriter(new FileWriter(destino))) {

            while (raf.getFilePointer() < raf.length()) {

                for (CampoDefinicion c : esquema.getCampos()) {

                    String clave = c.getNombre();
                    String valor;

                    switch (c.getTipoCampo()) {

                        case ENTERO:

                            valor = Integer.toString(raf.readInt());

                            break;

                        case CADENA:

                            valor = leerCadenaFija(raf, c.getLongitud());

                            break;

                        default:

                            throw new IOException("Tipo no soportado en el esquema: " + c.getTipoCampo());

                    }

                    bw.write(clave + "=" + valor);
                    bw.newLine();

                }
            }

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO,"DAT convertido correctamente a TXT: " + destino.getAbsolutePath());

        } catch (IOException e) {

            throw new RuntimeException("Error durante la conversión DAT a TXT", e);

        }
    }

    /**
     * Método que transforma un archivo XML en un fichero .txt usando DOM
     *
     * @param rutaUsuario  ruta del XML origen
     * @param ficheroNuevo nombre base del fichero .txt destino
     *
     * @throws IllegalArgumentException si el fichero no existe o no es XML
     * @throws RuntimeException en caso de que ocurra un error durante la conversión
     */
    public void transformarXml2Txt(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);
        File destino = new File(ficheroNuevo + ".txt");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero no existe o no es válido: " + rutaUsuario);

        }

        if (!rutaUsuario.toLowerCase().endsWith(".xml")) {

            throw new IllegalArgumentException("Debe indicar un fichero XML válido.");

        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destino))){

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document documento = builder.parse(rutaUsuario);

            documento.getDocumentElement().normalize();

            NodeList listaObjeto = documento.getElementsByTagName("persona");
            
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

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO,"XML convertido correctamente a TXT: " + destino.getAbsolutePath());

        } catch (ParserConfigurationException | SAXException e) {

            throw new RuntimeException("Error interpretando el XML de entrada", e);

        } catch (IOException e) {

            throw new RuntimeException("Error leyendo o escribiendo ficheros durante la conversión", e);

        }
        
    }

    /**
     * Convierte un XML en un fichero .dat, escribiendo cada elemento como un registro fijo
     *
     * @param rutaUsuario  ruta del XML origen
     * @param ficheroNuevo nombre base del .dat destino
     *
     * @throws IllegalArgumentException si el XML no existe o no es válido
     * @throws RuntimeException si ocurre un error de lectura o escritura
     */
    public void transformarXml2Dat(String rutaUsuario, String ficheroNuevo) {

        final int LONG_NOMBRE = 20;
        
        File origen = new File(rutaUsuario);
        File destino = new File(ficheroNuevo + ".dat");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero XML no existe o no es un archivo válido: " + rutaUsuario);

        }

        if (!rutaUsuario.toLowerCase().endsWith(".xml")) {

            throw new IllegalArgumentException("El fichero origen debe ser XML: " + rutaUsuario);

        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(destino))) {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(origen);
            doc.getDocumentElement().normalize();

            Element raiz = doc.getDocumentElement();
            NodeList registros = raiz.getChildNodes();

            for (int i = 0; i < registros.getLength(); i++) {

                Node nodo = registros.item(i);

                if (nodo.getNodeType() != Node.ELEMENT_NODE) {

                    continue;

                }

                Element elemento = (Element) nodo;

                String idStr = obtenerValorHijo(elemento, "id");
                String nombre = obtenerValorHijo(elemento, "nombre");

                int id = Integer.parseInt(idStr);

                dos.writeInt(id);

                escribirCadenaFija(dos, nombre, LONG_NOMBRE);

            }

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO,"XML convertido a DAT correctamente: " + destino.getAbsolutePath());

        } catch (IOException | ParserConfigurationException | SAXException e) {

            throw new RuntimeException("Error durante la conversión XML a DAT", e);

        }
        
    }

    /**
     * Devuelve el texto del elemento hijo con la etiqueta indicada.
     */
    public String obtenerValorHijo(Element padre, String tag) {

        NodeList lista = padre.getElementsByTagName(tag);

        if (lista.getLength() == 0) {

            return "";

        }

        return lista.item(0).getTextContent().trim();

    }

    /**
     * Escribe una cadena fija de longitud dada en un DataOutputStream,
     * rellenando con 0 si es más corta.
     */
    public void escribirCadenaFija(DataOutputStream dos, String texto, int longitud) throws IOException {

        int i = 0;

        while (i < texto.length() && i < longitud) {

            char c = texto.charAt(i);
            dos.writeByte((byte) c);

            i++;

        }

        // Rellenar con 0
        while (i < longitud) {

            dos.writeByte(0);

            i++;

        }
    }


    /**
    * Crea fichero .properties por cada registro del fichero .dat
    * Estructura por registro: id (int = 4) y nombre (cadena fija de 20 bytes)
    * El nombre de salida será: <ficheroNuevo><id>.properties en la misma carpeta del origen
     *
    * @param rutaUsuario ruta relativa del archivo .dat
    * @param ficheroNuevo prefijo del fichero nuevo
    */
   public void transformarDat2Properties(String rutaUsuario, String ficheroNuevo) {

       final int LONG_NOMBRE = 20;

       File origen = new File(rutaUsuario);

       File parent = origen.getParentFile();

       if (parent == null) {

           parent = new File(".");

       }

       File destino = new File(parent, ficheroNuevo + ".properties");

       if (!origen.exists() || !origen.isFile()) {

           throw new IllegalArgumentException("El fichero origen no existe o no es válido: " + rutaUsuario);

       }
       if (!rutaUsuario.toLowerCase().endsWith(".dat")) {

           throw new IllegalArgumentException("El fichero debe tener extensión .dat: " + rutaUsuario);

       }

       try (RandomAccessFile raf = new RandomAccessFile(origen, "r")) {

           while (raf.getFilePointer() < raf.length()) {

               int id = raf.readInt();

               String nombre = leerCadenaFija(raf, LONG_NOMBRE);

               Properties props = new Properties();
               props.setProperty("id", Integer.toString(id));
               props.setProperty("nombre", nombre);

               File archivoSalida = new File(destino, ficheroNuevo + id + ".properties");

               try (FileOutputStream fos = new FileOutputStream(archivoSalida)) {

                   props.store(fos, "Exportado desde fichero DAT");

               }

               System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO,"Generado fichero: " + archivoSalida.getAbsolutePath());

           }

       } catch (IOException e) {

           throw new RuntimeException("Error de E/S durante la conversión de DAT a properties", e);

       } catch (Exception e) {

           throw new RuntimeException("Error en la conversión DAT a properties", e);

       }
   }

    /**
    * Convierte un XML a varios .properties (uno por cada elemento-hijo del root)
    * Ejemplo de salida: <ficheroNuevo>1.properties, <ficheroNuevo>2.properties...
    *
    * @param rutaUsuario ruta del XML de origen
    * @param ficheroNuevo  prefijo para los .properties de salida
    */
    public void transformarXml2Properties(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);

        File parent = origen.getParentFile();

        if (parent == null) {

            parent = new File("."); // Si el archivo está en directorio actual, entonces toma "." como directorio

        }

        File destino = new File(parent, ficheroNuevo + ".properties");

        if (!origen.exists() || !origen.isFile()) {

            throw new IllegalArgumentException("El fichero proporcionado no existe: " + rutaUsuario);

        }

        if (!rutaUsuario.toLowerCase().endsWith(".xml")) {

            throw new IllegalArgumentException("El fichero origen debe ser XML: " + rutaUsuario);

        }

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(destino);
            xml.getDocumentElement().normalize();

            Element root = xml.getDocumentElement();
            NodeList elementos = root.getChildNodes();

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

                    if (hijo.getNodeType() != Node.ELEMENT_NODE) {
                        
                        continue;
                        
                    }

                    String clave = hijo.getNodeName();
                    String valor = hijo.getTextContent().trim();
                    
                    if (!valor.isEmpty()) {
                        
                        props.setProperty(clave, valor);
                        
                    }
                }

                if (!props.isEmpty()) {
                    
                    File salida = new File(destino, ficheroNuevo + contador + ".properties");
                    
                    try (FileOutputStream fos = new FileOutputStream(salida)) {
                        
                        props.store(fos, "Generado desde XML");
                        
                    }

                    System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO,"Generado fichero: " + salida.getAbsolutePath());

                    contador++;

                }
            }

        } catch (ParserConfigurationException | SAXException e) {

            throw new RuntimeException("Error analizando el XML: " + origen.getName(), e);

        } catch (IOException e) {

            throw new RuntimeException("Error leyendo o escribiendo ficheros", e);

        } catch (Exception e) {

            throw new RuntimeException("Error inesperado durante la conversión XML a properties", e);

        }
    }

    /**
     * Convierte un archivo .dat en un .xml usando DOM
     * Cada registro está compuesto por un ID (int = 4 bytes) y nombre (cadena fija 20 bytes)
     *
     * @param rutaUsuario ruta del fichero .dat origen
     * @param ficheroNuevo nombre base del XML destino
     *
     * @throws IllegalArgumentException si el fichero no existe o no es .dat
     * @throws RuntimeException si ocurre un error de lectura o transformación
     */
    public void transformarDat2Xml(String rutaUsuario, String ficheroNuevo) {

        File origen = new File(rutaUsuario);
        File destino = new File(origen.getParent(), ficheroNuevo + ".xml");
        final int LONG_NOMBRE = 20;

        try (RandomAccessFile raf = new RandomAccessFile(origen, "r")) {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element root = doc.createElement("alumnos");
            doc.appendChild(root);

            while (raf.getFilePointer() < raf.length()) {

                int id = raf.readInt();
                String nombre = leerCadenaFija(raf, LONG_NOMBRE);

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

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO,"DAT convertido a XML correctamente: " + destino.getAbsolutePath());

        } catch (Exception e) {
            
            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            System.err.println("Error al convertir DAT a XML: " + e.getMessage());
            
        }
    }

    /**
     * Lee una cadena de longitud fija desde un RandomAccessFile
     *
     * @param raf fichero aleatorio desde el que leer
     * @param longitud número de bytes que ocupa la cadena fija
     * @return la cadena reconstruida
     * @throws IOException si ocurre un error al leer
     */
    public String leerCadenaFija(RandomAccessFile raf, int longitud) throws IOException {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < longitud; i++) {

            byte b = raf.readByte();  // lectura byte a byte

            if (b != 0 && b != 32) {

                sb.append((char) b);

            }
        }

        return sb.toString().trim();
    }


    /**
     * Clona un fichero XML origen a un nuevo fichero XML utilizando DOM
     *
     * @param rutaUsuario ruta del fichero XML origen
     * @param ficheroNuevo nombre base del fichero destino sin extensión
     *
     * @throws IllegalArgumentException si la ruta no existe o no es un XML
     * @throws RuntimeException si ocurre un error durante la lectura/parsing o escritura del XML
     */
    public void transformarXml2Xml(String rutaUsuario, String ficheroNuevo) {

        File file = new File(rutaUsuario);
        File newFile = new File(ficheroNuevo + ".xml");

        if (!file.exists() || !file.isFile()) {

            throw new IllegalArgumentException("La ruta indicada no existe o no es un fichero: " + rutaUsuario);

        }

        if (!rutaUsuario.toLowerCase().endsWith(".xml")) {

            throw new IllegalArgumentException("El fichero origen debe ser XML: " + rutaUsuario);

        }

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

            System.getLogger(FileConversor.class.getName()).log(System.Logger.Level.INFO, "XML clonado correctamente en ", newFile.getName());

        } catch (ParserConfigurationException | SAXException e) {

            throw new RuntimeException("Error al procesar el XML: " + file.getName(), e);

        } catch (IOException e) {

            throw new RuntimeException("Error de E/S en el fichero: " + file.getName(), e);

        } catch (TransformerException e) {

            throw new RuntimeException("Error al transformar o generar el XML", e);

        }
    }

}
