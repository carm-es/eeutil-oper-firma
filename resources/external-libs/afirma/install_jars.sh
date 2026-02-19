#mvn install:install-file -Dfile=./libs/0/miniapplet-full_1_5.jar -DgroupId=es.gob.afirma #-DartifactId=miniapplet-afirma -Dversion=5.0 -Dpackaging=jar
#mvn install:install-file -Dfile=./libs/0/miniapplet-full_1_6_5.jar -DgroupId=es.gob.afirma -DartifactId=miniapplet-afirma -Dversion=6.5 -Dpackaging=jar
#customizamos la libreria para borrar dos carpetas que dejaban temporales.


mvn install:install-file -Dfile=./libs/0/afirma-core-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-core -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-cades-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-cades -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-cades-multi-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-cades-multi -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-pdf-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-pdf -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-xades-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-xades -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-core-keystores-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-core-keystores -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-core-xml-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-core-xml -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-core-pkcs7-tsp-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-core-pkcs7-tsp -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-crypto-core-pkcs7-1.7.jar -DgroupId=es.gob.afirma -DartifactId=afirma-crypto-core-pkcs7 -Dversion=1.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-eeutils-compatibility-1.0.jar -DgroupId=es.gob.eeutils -DartifactId=afirma-eeutils-compatibility -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-lib-itext-1.3.jar -DgroupId=es.gob.afirma.lib -DartifactId=afirma-lib-itext -Dversion=1.3 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-lib-jmimemagic-0.0.6.jar -DgroupId=es.gob.afirma.lib -DartifactId=afirma-lib-jmimemagic -Dversion=0.0.6 -Dpackaging=jar
mvn install:install-file -Dfile=./libs/0/afirma-lib-oro-0.0.6.jar -DgroupId=es.gob.afirma.lib -DartifactId=afirma-lib-oro -Dversion=0.0.6 -Dpackaging=jar

