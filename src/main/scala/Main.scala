import java.io.File
import java.time.{Duration, LocalDateTime}

import cats.implicits._
import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import Utils._

object Main extends App {

  def read(path: String): Seq[In] = {
    val reader: CSVReader = CSVReader.open(new File(path))
    val entries = reader.all().map {
      case List(login, ip, timeStr) =>
        In(login, ip, LocalDateTime.parse(timeStr, datePattern))
    }
    reader.close()
    entries
  }

  def write(outs: Seq[Out], path: String = "out.csv"): Unit = {
    val lines: Seq[List[String]] = outs.map(
      out =>
        List(out.ip,
             out.start.show,
             out.stop.show,
             out.users.map(_.show).mkString(",")))
    val writer = CSVWriter.open(path, append = true)
    writer.writeAll(lines)
    writer.close()
  }

  def transform(ins: Seq[In], window: Int = 60): Seq[Out] = {
    ins
      .groupBy(_.ip)
      .filter {
        case (_, seq) => seq.size > 1
      }
      .map {
        case (ip, seq) =>
          val ens = seq
            .sliding(2, 1)
            .filter {
              case List(a, b) => Duration.between(a.time, b.time).toMinutes <= window
            }
            .flatten
            .toList
            .distinct
          (ip, ens)
      }
      .filter {
        case (_, ens) => ens.size > 1
      }
      .map {
        case (ip, seq) =>
          val users = seq.map(in => UserTime(in.login, in.time))
          Out(ip, seq.head.time, seq.last.time, users)
      }
      .toSeq
  }

  val ins = read("/Users/olzhas/Desktop/logins.csv")
  val outs = transform(ins)
  write(outs, "/Users/olzhas/Desktop/out.csv")
}
