
-- tabla `generadorclaves`
DROP TABLE IF EXISTS `generadorclaves`;

CREATE TABLE `generadorclaves` (
  `GenName` varchar(255) NOT NULL,
  `GenValue` int(11) NOT NULL,
  PRIMARY KEY (`GenName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--tabla `inside_aplicaciones`
DROP TABLE IF EXISTS `inside_aplicaciones`;

CREATE TABLE `inside_aplicaciones` (
  `idaplicacion` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `descripcion` varchar(1000) NOT NULL,
  `activa` varchar(1) NOT NULL DEFAULT 'S',
  `tramitar` varchar(1) NOT NULL DEFAULT 'S',
  `sello` varchar(1) NOT NULL DEFAULT 'S',
  `firma` varchar(1) NOT NULL DEFAULT 'S',
  `email` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `responsable` varchar(255) DEFAULT NULL,
  `unidad` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`idaplicacion`),
  KEY `eeutil_aplic_unidad_fk` (`unidad`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--tabla `inside_aplicaciones_propiedad`
DROP TABLE IF EXISTS `inside_aplicaciones_propiedad`;

CREATE TABLE `inside_aplicaciones_propiedad` (
  `idaplicacion` varchar(100) NOT NULL,
  `propiedad` varchar(100) NOT NULL,
  `valor` varchar(500) NOT NULL,
  PRIMARY KEY (`idaplicacion`,`propiedad`),
  CONSTRAINT `inside_aplicaciones_propiedad_FK` FOREIGN KEY (`idaplicacion`) REFERENCES `inside_aplicaciones` (`idaplicacion`),
  CONSTRAINT `inside_app_propiedad_FK` FOREIGN KEY (`idaplicacion`) REFERENCES `inside_aplicaciones` (`idaplicacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--tabla `peticiones_pdfa`
DROP TABLE IF EXISTS `peticiones_pdfa`;

CREATE TABLE `peticiones_pdfa` (
  `ID` bigint(20) NOT NULL,
  `idaplicacion` varchar(100) NOT NULL,
  `fecha_peticion` datetime NOT NULL,
  `numero_paginas` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `peticiones_pdfa_fk1` (`idaplicacion`),
  KEY `peticiones_pdfa_fecha_peticion_IDX` (`fecha_peticion`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


--tabla `usuario_credencial`
DROP TABLE IF EXISTS `usuario_credencial`;

CREATE TABLE `usuario_credencial` (
  `nif` varchar(100) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


--tabla `unidadorganica`
DROP TABLE IF EXISTS `unidadorganica`;

CREATE TABLE `unidadorganica` (
  `id` int(11) NOT NULL,
  `Codigo_Unidad_Organica` varchar(10) NOT NULL,
  `Nombre_Unidad_Organica` varchar(255) NOT NULL,
  `Nivel_Administracion` tinyint(4) NOT NULL,
  `ENTIDAD_DERECHO_PUBLICO` varchar(1) DEFAULT NULL,
  `Codigo_Externo` varchar(255) DEFAULT NULL,
  `Codigo_Unidad_Superior` varchar(10) NOT NULL,
  `Nombre_Unidad_Superior` varchar(255) NOT NULL,
  `CODIGO_UNIDAD_RAIZ` varchar(10) DEFAULT NULL,
  `NOMBRE_UNIDAD_RAIZ` varchar(255) DEFAULT NULL,
  `Codigo_Raiz_Derecho_Publico` varchar(10) DEFAULT NULL,
  `Nombre_Raiz_Derecho_Publico` varchar(255) DEFAULT NULL,
  `Nivel_Jerarquico` tinyint(4) NOT NULL,
  `Estado` varchar(1) NOT NULL,
  `Fecha_Alta` datetime DEFAULT NULL,
  `Fecha_Baja` datetime DEFAULT NULL,
  `Fecha_Anulacion` datetime DEFAULT NULL,
  `Fecha_Extincion` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `Version` varchar(3) DEFAULT NULL,
  `Version_URaiz` varchar(3) DEFAULT NULL,
  `Horario_Atenc` varchar(1024) DEFAULT NULL,
  `Ver_Un_SupORes` varchar(3) DEFAULT NULL,
  `Fecha_Ult_Act` datetime DEFAULT NULL,
  `Poder` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unidad_unica_idx` (`Codigo_Unidad_Organica`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--tabla `shedlock`
DROP TABLE IF EXISTS `shedlock`;

CREATE TABLE `shedlock` (
  `name` varchar(64) NOT NULL,
  `locked_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lock_until` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `locked_by` varchar(255) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- tabla `aplicacion_operacion`
DROP TABLE IF EXISTS `aplicacion_operacion`;

CREATE TABLE `aplicacion_operacion` (
  `id` int(11) NOT NULL,
  `idaplicacion` varchar(100) NOT NULL,
  `operacion` varchar(100) CHARACTER SET latin1 NOT NULL,
  `numero_peticiones` int(11) NOT NULL,
  `capturar` varchar(1) CHARACTER SET latin1 NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `aplicacion_operacion_FK` (`idaplicacion`),
  CONSTRAINT `aplicacion_operacion_FK` FOREIGN KEY (`idaplicacion`) REFERENCES `inside_aplicaciones` (`idaplicacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--tabla `inside_aplicaciones_plantillas`
DROP TABLE IF EXISTS `inside_aplicaciones_plantillas`;

CREATE TABLE `inside_aplicaciones_plantillas` (
  `idaplicacion` varchar(100) NOT NULL,
  `idplantilla` varchar(100) NOT NULL,
  `plantilla` blob NOT NULL,
  PRIMARY KEY (`idaplicacion`,`idplantilla`),
  CONSTRAINT `inside_aplicaciones_plantillas_FK` FOREIGN KEY (`idaplicacion`) REFERENCES `inside_aplicaciones` (`idaplicacion`),
  CONSTRAINT `inside_app_plantillas_FK` FOREIGN KEY (`idaplicacion`) REFERENCES `inside_aplicaciones` (`idaplicacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- tabla `eeutil_auditoria`
DROP TABLE IF EXISTS `eeutil_auditoria`;

CREATE TABLE `eeutil_auditoria` (
  `ID` bigint(20) NOT NULL,
  `APLICACION` varchar(100) DEFAULT NULL,
  `OPERACION` varchar(100) DEFAULT NULL,
  `FECHA` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `SERVICIO` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`,`FECHA`),
  KEY `EEUTIL_AUDITORIA_FECHA_IDX` (`FECHA`) USING BTREE,
  KEY `EEUTIL_AUDITORIA_APLICACION_IDX2` (`APLICACION`,`OPERACION`) USING HASH,
  KEY `EEUTIL_AUDITORIA_APP_IDX` (`APLICACION`),
  KEY `EEUTIL_AUDITORIA_APP_IDX2` (`APLICACION`,`OPERACION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


-- tabla `eeutil_auditoria_historico`
DROP TABLE IF EXISTS `eeutil_auditoria_historico`;

CREATE TABLE `eeutil_auditoria_historico` (
  `ID` int(11) NOT NULL,
  `APLICACION` varchar(100) DEFAULT NULL,
  `OPERACION` varchar(100) DEFAULT NULL,
  `FECHA` datetime DEFAULT NULL,
  `SERVICIO` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `EEUTIL_AUDITORIA_FECHA_IDX` (`FECHA`) USING BTREE,
  KEY `EEUTIL_AUDITORIA_APLICACION_IDX2` (`APLICACION`,`OPERACION`) USING HASH,
  KEY `EEUTIL_AUDITORIA_APP_IDX` (`APLICACION`),
  KEY `EEUTIL_AUDITORIA_APP_IDX2` (`APLICACION`,`OPERACION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- tabla `eeutil_cache`
DROP TABLE IF EXISTS `eeutil_cache`;

CREATE TABLE `eeutil_cache` (
  `IP_HOST` varchar(64) NOT NULL COMMENT 'IP del host a cachear',
  `MODULO` varchar(100) NOT NULL,
  `A_CACHEAR` int(11) NOT NULL DEFAULT '0',
  `FECHA_ALTA` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `FECHA_ULTIMO_CACHEO` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`IP_HOST`,`MODULO`),
  KEY `EEUTIL_CACHE_IP_HOST_IDX` (`IP_HOST`,`MODULO`,`A_CACHEAR`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Tabla que gestiona los cacheos';


--tabla `eeutil_run_count`
DROP TABLE IF EXISTS `eeutil_run_count`;

CREATE TABLE `eeutil_run_count` (
  `CONTADOR` int(11) NOT NULL DEFAULT '0',
  `FECHA_ULTIMA_ACT` date NOT NULL,
  `HOSTNAME` varchar(1024) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Tabla para contabilizar el numero de urls almacenadas en RUN';


-- Insert para añadir el DNI del usuario para acceder a EEUTIL-MISC via web
INSERT INTO distribuible.usuario_credencial (nif) VALUES('XXXX');


-- Insert para dar de alta por BBDD al idAplicacion consumidor(usuario) del servicio de EEUTILS
-- Configurado con para usuario: usuariolocal / contrasena: 1234 (codificacion de la contrasena en MD5)
INSERT INTO distribuible.inside_aplicaciones (idaplicacion,password,descripcion,activa,tramitar,sello,firma,email,telefono,responsable,unidad) VALUES
	 ('usuariolocal','81dc9bdb52d04dc20036dbd8313ed055','local','S','S','S','S','[direcccion_de_correo]','[numero_de_telefono]','[nombre_del_responsable]','[unidad_organica_dir3]');


-- Insert con los datos necesarios asociados al idAplicacion consumidor
INSERT INTO distribuible.inside_aplicaciones_propiedad (idaplicacion,propiedad,valor) VALUES
	 ('usuariolocal','algoritmoFirmaDefecto','SHA1withRSA'),
	 ('usuariolocal','aliasCertificado','XXXXX'),
	 ('usuariolocal','formatoFirmaDefecto','Adobe PDF'),
	 ('usuariolocal','ip.openoffice','XXXXX'),
	 ('usuariolocal','modoFirmaDefecto','implicit').
	 ('usuariolocal','passwordCertificado','<noaplica>'),
	 ('usuariolocal','passwordKS','XXXXX'),
	 ('usuariolocal','port.openoffice','XXXX'),
	 ('usuariolocal','rutaKS','${local_home_app}/XXXXXX'),
	 ('usuariolocal','rutaLogo','${local_home_app}/sgtic/conf/escudo.jpg');