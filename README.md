# Conversor de Ficheros (TXT / DAT / PROPERTIES / XML) + CRUD sobre DAT

Proyecto en Java que permite convertir ficheros entre formatos y gestionar registros binarios de tamaño fijo (.dat) desde un menú por consola.  
Incluye operaciones CRUD completas sobre archivos DAT con layout fijo.


## 📌 Características principales

    - Menú interactivo por consola
    - Conversión entre múltiples formatos
    - CRUD completo sobre archivos `.dat`
    - Parser de esquema para interpretar registros binarios
    - Implementaciones usando `RandomAccessFile`, DOM y `Properties`

## 📁 Formatos soportados

### **1. TXT**

    - **TXT → TXT** (copia exacta línea a línea)
    - **TXT → DAT** (copia binaria byte a byte)
    - **TXT → PROPERTIES**
    - Verifica formato `clave=valor`
    - Si no cumple avisa y no convierte

### **2. DAT (Registros de tamaño fijo)**

Layout fijo por defecto:

    id → int (4 bytes)
    nombre → String fijo 20 bytes (padding con espacios)
    activo → boolean (1 byte)

#### Operaciones CRUD disponibles

    - **AÑADIR** registro
    - **MODIFICAR** campo por id
    - **BORRADO LÓGICO** (activo = false)
    - **LEER** registro por id

#### Conversiones

    - **DAT → TXT** (usando esquema)
    - **DAT → PROPERTIES** (1 fichero por registro)
    - **DAT → XML** (DOM – según versión)

### **3. PROPERTIES**

Se generan desde:

    - TXT válido (`clave=valor`)
    - DAT (1 `.properties` por registro)
    - XML (solo etiquetas con valor)

### **4. XML**

    - **XML → TXT** (DOM/SAX según implementación)
    - **XML → PROPERTIES** (un `.properties` por elemento hijo)
    - **XML → XML** (clonado usando DOM)
    - **XML → DAT** (pendiente según versión)

## 📘 Esquema de Registros (.txt)

    Para poder interpretar un `.dat` se necesita un archivo de esquema, ya que el binario no contiene metadatos.

### Formato esperado:

    Alumno
    id int 4
    nombre String 20
    activo int 1

El programa obtiene de aquí:

    - nombre de entidad
    - lista de campos
    - tipo de dato (`int` / `String`)
    - tamaño en bytes

Esquema obligatorio para:

    - **DAT → TXT**
    - **DAT → XML**
    - **DAT → PROPERTIES**
    - **CRUD DAT**

## 🧩 Arquitectura del Proyecto

    src/main/java/com/mycompany/proyectojaime/
    │
    ├── app/
    │ └── ProyectoJaime.java → Menú principal (main)
    │
    ├── conversor/
    │ └── FileConversor.java → Métodos de conversión entre formatos
    │
    ├── dat/
    │ ├── DATController.java → CRUD DAT y utilidades RAF
    │ ├── EsquemaRegistro.java → Representa el esquema
    │ ├── CampoDefinicion.java → Campo de un registro
    │ └── TipoCampo.java → Tipos admitidos

## Ejecución

    1. Ejecutar `ProyectoJaime`
    2. Introducir ruta del archivo a convertir
    3. Si es `.dat`, indicar si el esquema es conocido
    4. Elegir tipo de salida desde el menú
    5. Introducir nombre del nuevo fichero



## Ficheros de prueba recomendados

    datos/
    ├── alumnos.dat
    ├── esquema_alumno.txt
    ├── personas.xml
    ├── config.txt
    ├── malo.txt
    └── ejemplo.properties


## 📝 Autor

**Jaime Pérez Roget Blanco**

08/10/2025
