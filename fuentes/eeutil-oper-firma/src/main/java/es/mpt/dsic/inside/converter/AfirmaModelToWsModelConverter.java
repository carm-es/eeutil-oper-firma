/*
 * Copyright (C) 2012-13 MINHAP, Gobierno de España This program is licensed and may be used,
 * modified and redistributed under the terms of the European Public License (EUPL), either version
 * 1.1 or (at your option) any later version as soon as they are approved by the European
 * Commission. Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * more details. You should have received a copy of the EUPL1.1 license along with this program; if
 * not, you may find it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.mpt.dsic.inside.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import es.mpt.dsic.inside.model.ConfiguracionAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.ContenidoInfoAfirma;
import es.mpt.dsic.inside.model.FirmaInfoAfirma;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionFirmaFormatoAAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirma;
import es.mpt.dsic.inside.model.ResultadoValidarCertificadoAfirma;
import es.mpt.dsic.inside.model.TipoDeFirmaAfirma;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.ContenidoInfo;
import es.mpt.dsic.inside.ws.service.model.FirmaInfo;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InfoDetalladaCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFormatoAInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;
import es.mpt.dsic.inside.ws.service.model.TipoDeFirma;

public class AfirmaModelToWsModelConverter {

  public static ResultadoValidacionInfo resultadoValidacionInfoAfirmaToResultadoValidacionInfo(
      ResultadoValidacionInfoAfirma resAfirma) {
    if (resAfirma == null) {
      return null;
    }

    ResultadoValidacionInfo resultado = new ResultadoValidacionInfo();
    resultado.setEstado(resAfirma.isEstado());
    resultado.setDetalle(resAfirma.getDetalle());

    return resultado;

  }

  public static InformacionFirma informacionFirmaAfirmaToInformacionFirma(
      InformacionFirmaAfirma infoAfirma) {

    if (infoAfirma == null) {
      return null;
    }

    InformacionFirma info = new InformacionFirma();
    info.setAlgoritmoHashFirmado(infoAfirma.getAlgoritmoHashFirmado());
    info.setDocumentoFirmado(contenidoInfoAfirmaToContenidoInfo(infoAfirma.getDocumentoFirmado()));
    info.setEsFirma(infoAfirma.isEsFirma());
    info.setFirmantes(listaFirmaInfoAfirmaToListaFirmaInfo(infoAfirma.getFirmantes()));
    info.setHashFirmado(infoAfirma.getHashFirmado());
    info.setTipoDeFirma(tipoDeFirmaAfirmaToTipoDeFirma(infoAfirma.getTipoDeFirma()));

    return info;

  }

  public static ContenidoInfo contenidoInfoAfirmaToContenidoInfo(ContenidoInfoAfirma contAfirma) {
    if (contAfirma == null) {
      return null;
    }

    ContenidoInfo cont = new ContenidoInfo();
    cont.setContenido(contAfirma.getContenido());
    cont.setTipoMIME(contAfirma.getTipoMIME());

    return cont;
  }

  public static ListaFirmaInfo listaFirmaInfoAfirmaToListaFirmaInfo(
      List<FirmaInfoAfirma> listaAfirma) {
    if (listaAfirma == null) {
      return null;
    }

    ListaFirmaInfo lista = new ListaFirmaInfo();
    lista.setInformacionFirmas(new ArrayList<FirmaInfo>());

    for (FirmaInfoAfirma firmaInfoAfirma : listaAfirma) {
      lista.getInformacionFirmas().add(firmaInfoAfirmaToFirmaInfo(firmaInfoAfirma));
    }
    return lista;

  }


  public static FirmaInfo firmaInfoAfirmaToFirmaInfo(FirmaInfoAfirma firmaInfoAfirma) {
    if (firmaInfoAfirma == null) {
      return null;
    }

    FirmaInfo firmaInfo = new FirmaInfo();
    firmaInfo.setNombre(firmaInfoAfirma.getNombre());
    firmaInfo.setApellido1(firmaInfoAfirma.getApellido1());
    firmaInfo.setApellido2(firmaInfoAfirma.getApellido2());
    firmaInfo.setNifcif(firmaInfoAfirma.getNifcif());
    firmaInfo.setFecha(firmaInfoAfirma.getFecha());
    firmaInfo.setExtras(firmaInfoAfirma.getExtras());

    return firmaInfo;
  }

  public static TipoDeFirma tipoDeFirmaAfirmaToTipoDeFirma(TipoDeFirmaAfirma tipoAfirma) {
    if (tipoAfirma == null) {
      return null;
    }
    TipoDeFirma tipo = new TipoDeFirma();
    tipo.setModoFirma(tipoAfirma.getModoFirma());
    tipo.setTipoFirma(tipoAfirma.getTipoFirma());

    return tipo;
  }

  public static ResultadoValidarCertificado resultadoValidarCertificadoAfirmaToResultadoValidarCertificado(
      ResultadoValidarCertificadoAfirma resAfirma) {
    if (resAfirma == null) {
      return null;
    }
    ResultadoValidarCertificado res = new ResultadoValidarCertificado();
    res.setValidado(resAfirma.isValidado());
    res.setDetalleValidacion(resAfirma.getDetalleValidacion());
    res.setIdUsuario(resAfirma.getIdUsuario());
    res.setNumeroSerie(resAfirma.getNumeroSerie());

    return res;
  }

  @SuppressWarnings("rawtypes")
  public static InfoCertificado resultadoValidarCertificadoAfirmaToResultadoValidarCertificadoAmpliado(
      ResultadoValidarCertificadoAfirma resAfirma) {
    if (resAfirma == null) {
      return null;
    }
    InfoCertificado res = new InfoCertificado();
    res.setValidado(resAfirma.isValidado());
    res.setDetalleValidacion(resAfirma.getDetalleValidacion());
    res.setIdUsuario(resAfirma.getIdUsuario());
    res.setNumeroSerie(resAfirma.getNumeroSerie());

    if (resAfirma.getInfoCertificado() != null) {
      List<InfoDetalladaCertificado> detalle = new ArrayList<InfoDetalladaCertificado>();
      for (Entry entry : resAfirma.getInfoCertificado().entrySet()) {
        detalle
            .add(new InfoDetalladaCertificado((String) entry.getKey(), (String) entry.getValue()));
      }
      res.setInformacionDetallada(detalle);
    }

    return res;
  }

  public static ResultadoAmpliarFirma resultadoAmpliarFirmaAfirmaToResultadoAmpliarFirma(
      ResultadoAmpliarFirmaAfirma resAfirma) {
    if (resAfirma == null) {
      return null;
    }
    ResultadoAmpliarFirma res = new ResultadoAmpliarFirma();

    res.setFirma(resAfirma.getFirma());

    return res;

  }

  public static ConfiguracionAmpliarFirmaAfirma configuracionAmpliarFirmaToConfiguracionAmpliarFirmaAfirma(
      ConfiguracionAmpliarFirma conf) {
    if (conf == null) {
      return null;
    }

    ConfiguracionAmpliarFirmaAfirma confAfirma = new ConfiguracionAmpliarFirmaAfirma();

    if (conf.getCertificadosFirmantes() != null
        && conf.getCertificadosFirmantes().getCertificates() != null) {
      confAfirma.setCertificadosFirmantes(conf.getCertificadosFirmantes().getCertificates());
    }
    confAfirma.setFormatoAmpliacion(conf.getFormatoAmpliacion());
    confAfirma.setIgnorarPeriodoDeGracia(conf.isIgnorarPeriodoDeGracia());

    return confAfirma;
  }

  public static ResultadoValidacionFormatoAInfo resultadoValidacionFirmaFormatoAAfirmaToResultadoValidacionFormatoAInfo(
      ResultadoValidacionFirmaFormatoAAfirma resAfirma) {
    if (resAfirma == null) {
      return null;
    }
    ResultadoValidacionFormatoAInfo resultado = new ResultadoValidacionFormatoAInfo();
    resultado.setEstado(resAfirma.isEstado());
    resultado.setDetalle(resAfirma.getDetalle());
    resultado.setFechaValidezCertificadoTSA(resAfirma.getFechaValidezCertificadoTSA());
    return resultado;
  }

}
