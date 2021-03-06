/*
 * Author: Pablo Lalloni <plalloni@gmail.com>
 * Created: 02/06/2011 11:14:35
 */
package org.retistruen.survey

import org.joda.time.{Instant, Period, ReadablePeriod}
import org.retistruen._
import scala.actors.{Actor, TIMEOUT}

case object Stop

trait Chronometric {
  val instant = new Instant
}

case class Tick() extends Chronometric

class TickTimer(target: Actor, period: ReadablePeriod) extends Actor {

  private val millis = period.toPeriod.toStandardDuration.getMillis

  def act = loop {
    reactWithin(millis) {
      case TIMEOUT ⇒ target ! new Tick
      case Stop    ⇒ exit
    }
  }

  def stop =
    this ! Stop

}

case class Beat() extends Chronometric

class FrequencySurvey(target: Source[Int], period: ReadablePeriod, tagging: Option[Tagging] = None) extends Actor {

  private val duration = period.toPeriod.toStandardDuration

  private var beats: Seq[Beat] = Seq.empty

  private val timer = new TickTimer(this, period)

  timer.start

  def act = loop {
    react {
      case tick: Tick ⇒
        beats = beats.filter(_.instant isAfter tick.instant.minus(duration))
        target << Datum(beats.count(_.instant isBefore tick.instant), tick.instant, tagging)
      case beat: Beat ⇒
        beats :+= beat
      case Stop ⇒
        timer.stop
        exit
    }
  }

  def beat =
    this ! new Beat

  def stop =
    this ! Stop

}
