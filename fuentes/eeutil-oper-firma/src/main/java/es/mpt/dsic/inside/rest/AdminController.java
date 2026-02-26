/*
 * Copyright (C) 2025, Gobierno de España This program is licensed and may be used, modified and
 * redistributed under the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European Commission. Unless
 * required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and more details. You
 * should have received a copy of the EUPL1.1 license along with this program; if not, you may find
 * it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.mpt.dsic.inside.rest;

import java.text.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import es.mpt.dsic.inside.utils.UtilidadesTestEeutilOperFirma;
import es.mpt.dsic.inside.utils.test.IPruebaTraza;
import es.mpt.dsic.inside.utils.test.UtilidadesTestAfirmawsStub;
import es.mpt.dsic.inside.utils.test.UtilidadesTestComunes;
import es.mpt.dsic.inside.utils.test.UtilidadesTestModel;
import es.mpt.dsic.inside.utils.test.UtilidadesTestSchredule;
import es.mpt.dsic.inside.utils.test.UtilidadesTestServices;
import es.mpt.dsic.inside.utils.test.UtilidadesTestUtil;

@Controller
@RequestMapping("administracion")
public class AdminController extends BaseController {

  protected static final Log logger = LogFactory.getLog(AdminController.class);


  @RequestMapping(value = "/testtrazas", method = RequestMethod.GET)
  public String testTrazas(ModelMap model) throws ParseException {

    String resultado = "";
    String saltoLinea = "<br>";
    // eeutil-util
    IPruebaTraza iPT = new UtilidadesTestUtil();
    resultado += iPT.testTrazaImp() + saltoLinea;
    // eeutil-afirma-stub
    iPT = new UtilidadesTestAfirmawsStub();
    resultado += iPT.testTrazaImp() + saltoLinea;
    // eeutil-comunes
    iPT = new UtilidadesTestComunes();
    resultado += iPT.testTrazaImp() + saltoLinea;
    // eeutil-model
    UtilidadesTestModel umodel = new UtilidadesTestModel();
    resultado += umodel.testModel() + saltoLinea;
    // eeutil-schredule
    iPT = new UtilidadesTestSchredule();
    resultado += iPT.testTrazaImp() + saltoLinea;
    // eeutil-services
    iPT = new UtilidadesTestServices();
    resultado += iPT.testTrazaImp() + saltoLinea;
    // eeutil-firma
    iPT = new UtilidadesTestEeutilOperFirma();
    resultado += iPT.testTrazaImp() + saltoLinea;

    model.addAttribute("title", "Gestion de trazas en eeutil-firma");
    model.addAttribute("data", resultado);
    model.addAttribute("vmaven", getVersion());


    return "trazas";
  }



}
