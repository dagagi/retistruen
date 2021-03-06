package org.retistruen.instrument

import org.retistruen._

/** Records data received.
  * Optionally records a sliding window of the last *capacity* values received.
  * @tparam T  the type of the values received and recorded */
class RecordingReceiver[@specialized T](val name: String, val capacity: Option[Int] = None) extends Receiver[T] with Reset {

  private var buffer: Seq[Datum[T]] = Seq.empty

  def data = buffer

  def dataValues = buffer.map(_.value)

  override def receive(emitter: Emitter[T], datum: Datum[T]) = {
    buffer = buffer :+ datum
    for (c ← capacity) buffer = buffer.drop(buffer.size - c)
  }

  def reset {
    buffer = Seq.empty
  }

}
