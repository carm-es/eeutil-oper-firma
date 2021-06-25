# Eeutil-Oper-Firma
Eeutil-Oper-Firma

Algunos de los servicios que forman parte de esta aplicación son usados por la aplicación INSIDE, los cuales se identificacán a continuación junto con la información relacionada con cada uno de ellos.

## validacionFirma
Servicio de Eeutil-oper-firma que se utiliza cuando se valida en INSIDE un documento o un expediente ENI. Es llamado a través de la aplicación eeutil-misc y la llamada se realiza en InfoFirmaServiceImpl.java

* **aplicacionInfo:** Credenciales de la aplicación consumidora. 
* **Firma:** Firma en base64 que se quiere validar.
* **TipoFirma:** Tipo de la firma que se quiere validar. Es opcional. Las cadenas admitidas son:
*XAdES Detached
CAdES
Adobe PDF.*

## validarCertificado
Servicio de Eeutil-oper-firma que se utiliza cuando se accede a la web de INSIDE mediante la opción de certificado digital.

* **aplicacionInfo:** Credenciales de la aplicación consumidora. 
* **certificate:** Certificado a validar en base64

## obtenerInformacionFirma
Servicio de Eeutil-oper-firma que se utiliza cuando se genera un documento ENI y se seleccina la opción de *Firma, Archivo firmado previamente o Fimar en servidor y chequear firma longeva*. Posterior a este servicio se ejecuta el ampliarFirma en el mismo flujo. 

* **aplicacionInfo:** Credenciales de la aplicación consumidora. 
* **Firma:** Firma en base64 de la que se quiere obtener información.
* **opcionesObtenerInformacionFirma:** *obtenerFirmantes: true si se quieren obtener los firmantes, false en caso contrario. obtenerDatosFirmados: true si se quieren obtener los datos firmados, false en caso contrario. obtenerTipoFirma: true si se quiere obtener el tipo de firma, false en caso contrario.*

## ampliarFirma
Definición de los campos según guía de la AGE:

* **aplicacionInfo:** Credenciales de la aplicación consumidora. 
* **Firma:** Firma en base 64 que se desea ampliar.
* **configuracionAmpliarFirma:** formatoAmpliación: *Es el formato de ampliación deseado. Las cadenas admitidas son las aceptadas por el servicio DSSAfirmaVerify para el nodo SignatureForm:*

Este servicio no funciona actualmente en desarrollo ni en local cuando se accede por clave (al acceder por certificado digital el flujo de ejecución es distinto y no se ejecuta el servicio), se recibe como respuesta un error inesperado desde Afirma (en la web aparece mensaje de error: "Se produjo un error al intentar ampliar firma como longeva. Inténtelo pasados unos minutos" . Para ejecutarlo es necesario generar un Nuevo Documento seleccionado en *Firma, Archivo firmado previamente o Fimar en servidor y chequear firma longeva.
