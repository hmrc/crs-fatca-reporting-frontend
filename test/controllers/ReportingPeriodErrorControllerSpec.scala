package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ReportingPeriodErrorView

import java.time.{LocalDate, ZoneId}

class ReportingPeriodErrorControllerSpec extends SpecBase {

  "ReportingPeriodError Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val currentYear = LocalDate.now(ZoneId.of("Europe/London")).getYear
      val expectedMessage = s"The ReportingPeriod element in your file must contain a date between 31 December 2014 and 31 December ${currentYear}."

      running(application) {
        val request = FakeRequest(GET, routes.ReportingPeriodErrorController.onPageLoad().url)

        val result = route(application, request).value


        status(result) mustEqual OK
        contentAsString(result) must include(expectedMessage)
      }
    }
  }
}
