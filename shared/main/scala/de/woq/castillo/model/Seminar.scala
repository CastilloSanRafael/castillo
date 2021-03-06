package de.woq.castillo.model

case class Seminar (
  id          : String,          // A unique identifier
  details     : SeminarDetails   // The seminar details
)

case class SeminarDetails(
  title       : String,          // A seminar title
  description : String,          // The course content
  trainer     : String,          // Who is the trainer
  duration    : Int              // The course duration in days
)
