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

package es.mpt.dsic.inside.utils.test.reflect;

/**
 * Objetivos: 1. Conseguir acceder a la edad de la persona. 2. Conseguir que la edad de la persona
 * sea 25 y acceder a esa edad. 3. Construir dos objetos donde equals de valor true 4. Construir dos
 * objetos exactamente iguales donde la comparacion de objetos demuestre que no son los mismos.
 * 
 * @author miguel.moral
 *
 */
public class MockClassPersonaWrapper {


  protected MockClassPersonaWrapper(MockPersona p) {
    this.p = p;
  }


  private Integer getEdad() {
    return p.getEdad();
  }

  private MockPersona p;



}


