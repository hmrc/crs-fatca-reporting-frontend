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
