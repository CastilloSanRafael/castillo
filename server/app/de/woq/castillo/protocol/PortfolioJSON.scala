package de.woq.castillo.protocol

import de.woq.castillo.model.{Seminar, SeminarDetails}
import play.api.libs.json._
import Reads._
import play.api.libs.functional.syntax._

object PortfolioJSON {

  implicit val readsSeminarDetails : Reads[SeminarDetails] = (
    ( __ \ "title").read(minLength[String](1)) and
    ( __ \ "description").read(minLength[String](1)) and
    ( __ \ "trainer").read(minLength[String](1)) and
    ( __ \ "duration").read(min[Int](1))
  )(SeminarDetails)

  implicit val writesSeminarDetails = Json.writes[SeminarDetails]

  implicit val writesSeminar = Json.writes[Seminar]

  implicit val readsSeminar : Reads[Seminar] = (
    ( __ \ "id").read(min[Long](1)) and
    ( __  \ "details").read[SeminarDetails]
  )(Seminar)
}
