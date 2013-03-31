package com.phyous.grasp

import com.leapmotion.leap._

object App {

  case class Config(xSens: Double = 3.0, ySens: Double = 5.0, invertY: Boolean = false)

  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("grasp", "1.0") {
      def options = Seq(
        doubleOpt("x", "xSens", "X Sensitivity") {
          (v: Double, c: Config) => c.copy(xSens = v)
        },
        doubleOpt("y", "ySens", "Y Sensitivity") {
          (v: Double, c: Config) => c.copy(ySens = v)
        },
        booleanOpt("iy", "invertY", "Invert Y movement") {
          (v: Boolean, c: Config) => c.copy(invertY = v)
        }
      )
    }

    parser.parse(args, Config()) map { config =>
      GraspListener.setXSensitivity(config.xSens.asInstanceOf[Float])
      GraspListener.setYSensitivity(config.ySens.asInstanceOf[Float])
      GraspListener.invertY(config.invertY)

      val listener = new GraspListener
      val controller = new Controller
      controller.addListener(listener)
      println("Press Enter to quit...")
      System.in.read
      controller.removeListener(listener)
    }

  }

}
