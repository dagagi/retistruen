package org.retistruen.instrument

import org.retistruen._
import akka.actor.Actor
import akka.actor.Actor._
import grizzled.slf4j.Logging

class Joiner[T](val name: String)
    extends Receiver[T]
    with CachingEmitter[Seq[Datum[T]]]
    with Start with Stop with Reset with Logging {

  private case class Reception(emitter: Emitter[T], datum: Datum[T])

  private class Worker extends Actor {

    private[this] var data: Map[Emitter[T], Datum[T]] = Map.empty

    def receive = {
      case Reception(emitter, datum) ⇒
        data = data + (emitter → datum)
        val values = sources.map(data.get(_))
        if (!values.contains(None)) emit(Datum(values.map(_.get)))
      case Reset ⇒
        data = Map.empty
    }

  }

  val worker = actorOf(new Worker)

  def receive(emitter: Emitter[T], datum: Datum[T]): Unit = {
    if (!worker.isRunning) error("Joiner not started. Have you started your retistruen Model?")
    else if (!sources.contains(emitter)) error("Emitter unknown: " + emitter)
    else worker ! Reception(emitter, datum)
  }

  def start = worker.start

  def stop = worker.stop

  override def reset {
    super.reset
    worker ! Reset
  }

}
