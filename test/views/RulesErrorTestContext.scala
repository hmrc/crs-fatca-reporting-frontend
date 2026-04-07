/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

trait RulesErrorTestContext {

  def tableRows: Seq[Seq[String]] = Seq(
    Seq(
      "CRS 1",
      "GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001",
      """CrsBody must not contain:
                                                                             |
                                                                             |-- (double dash)
                                                                             |&# (ampersand hash)
                                                                             |/* (slash asterisk)
                                                                             |MessageRefId and DocRefId must not contain:
                                                                             |
                                                                             |' (apostrophe)
                                                                             |" (quotation mark)
                                                                             |& (ampersand)
                                                                             |< (less than)
                                                                             |> (greater than)
                                                                             |If elsewhere in the CrsBody, replace:
                                                                             |
                                                                             |' (apostrophe) with &apos;
                                                                             |" (quotation mark) with &quot;
                                                                             |& (ampersand) with &amp;
                                                                             |< (less than) with &lt;
                                                                             |> (greater than) with &gt;""".stripMargin
    )
  )
}
