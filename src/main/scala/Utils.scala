import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cats._
import cats.implicits._

object Utils {
  val datePattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  implicit val dateShow: Show[LocalDateTime] =
    Show.show[LocalDateTime](date => date.format(datePattern))
  implicit val userTimeShow: Show[UserTime] =
    Show.show[UserTime](u => s"${u.login}:${u.time.show}")
}

case class In(login: String, ip: String, time: LocalDateTime)
case class Out(ip: String,
               start: LocalDateTime,
               stop: LocalDateTime,
               users: Seq[UserTime])
case class UserTime(login: String, time: LocalDateTime)
