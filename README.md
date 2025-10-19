# Conversor de ficheros (TXT / DAT / PROPERTIES / XML) + CRUD DAT

Pequeño proyecto para convertir entre formatos y gestionar un .dat de registros de tamaño fijo. Todo desde un menú por consola y con código simple.

# Funcionalidades

Menú principal para convertir a:

TXT • DAT • PROPERTIES • XML

TXT

TXT → TXT (copia)

TXT → DAT (binario byte a byte)

TXT → PROPERTIES (valida clave=valor)

DAT

CRUD sobre layout fijo:
id (int 4 bytes) + nombre (20 bytes fijo, padding) + activo (boolean 1 byte)

DAT → TXT (con esquema)

DAT → PROPERTIES (pendiente según versión)

DAT → XML (DOM/XStream – pendiente según versión)

XML

XML → TXT (DOM/SAX según versión)

XML → PROPERTIES (un .properties por elemento hijo, solo etiquetas con valor)

XML → XML (clonado DOM)

XML → DAT (pendiente según versión)

Esquema de registros

Parser sencillo (asumiendo formato correcto) para líneas del tipo:

alumno
Id int 4
Nombre string 20

