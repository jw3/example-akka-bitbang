package pigpio.examples.serial


object foo extends App {

  val str = "hello"

  str.foreach { ch =>
    println(ch)
    println("\tlittle endian")
    for (i <- 0 to 7) {
      val bit = (ch.toInt >> i) & 1
      println(s"\t\tbit $i = $bit")
    }
    println("\tbig endian")
    for (i <- 0 to 7) {
      val bit = (ch.toInt << i) & 0x80
      println(s"\t\tbit $i = ${abit(bit)}")
    }
    println("\tbinary formatted")
    val binary = ch.toInt.toBinaryString.reverse.padTo(8, '0').reverse
    println(s"\t\t${ch.toInt.toBinaryString.reverse.padTo(8, '0').reverse}")

    val de = binary.map {
      case '1' => 1
      case '0' => 0
    }.foldLeft(0)((r, b) => r << 1 | b)

    println("\tbinary deserialized")
    println(s"\t\t${de.toChar}")
  }


  def abit(int: Int) = int match {
    case 0 => 0
    case _ => 1
  }
}
