package com.phyous.grasp

import scala.collection.JavaConverters._
import com.leapmotion.leap._
import java.awt.{Toolkit, Robot}
import java.awt.event.InputEvent
import java.util.concurrent.atomic.AtomicBoolean

object GraspListener {
  private val robot = new Robot()
  private val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
  private[this] val width = screenSize.getWidth.asInstanceOf[Int]
  private[this] val height = screenSize.getHeight.asInstanceOf[Int]
  private[this] var xSensistivity = 1.0
  private[this] var ySensistivity = 1.0
  private[this] var invertY: Boolean = true

  def setXSensitivity(xSens: Float) {
    xSensistivity = xSens
  }

  def setYSensitivity(ySens: Float) {
    ySensistivity = ySens
  }

  def invertY(invert: Boolean) {
    invertY = invert
  }

  private def setMouseCenter = {
    robot.mouseMove(width / 2, height / 2)
  }

  private def setRelativePosition(x: Float, y: Float) = {
    val xPos = (width / 2) + x * xSensistivity * (width / 2)
    val yOffset = y * ySensistivity * (height / 2)
    val yPos = if (invertY) {
      (height / 2) + yOffset
    } else {
      (height / 2) - yOffset
    }
    robot.mouseMove(xPos.asInstanceOf[Int], yPos.asInstanceOf[Int])
  }

  private def clickMouse() = {
    robot.mousePress(InputEvent.BUTTON1_MASK);
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
    println("Click activated")
  }
}

class GraspListener extends Listener {
  var handDetected = new AtomicBoolean()
  import GraspListener._

  def setHandDetected() {
    if (handDetected.compareAndSet(false, true)) {
      println("Hand detected")
    }
  }

  def setHandLost() {
    if (handDetected.compareAndSet(true, false)) {
      println("Hand lost")
    }
  }

  override def onInit(controller: Controller) {
    println("Initialized")
  }

  override def onConnect(controller: Controller) {
    println("Connected")
    controller.enableGesture(Gesture.Type.TYPE_KEY_TAP)
    setMouseCenter
  }

  override def onDisconnect(controller: Controller) {
    println("Disconnected")
  }

  override def onExit(controller: Controller) {
    println("Exited")
  }

  override def onFrame(controller: Controller) {
    val frame: Frame = controller.frame
    if (!frame.hands.empty) {
      setHandDetected
      def normalizeXPos(hand: Hand): Float = {
        (hand.palmPosition.getX / 100.0).asInstanceOf[Float]
      }
      val hand = frame.hands.get(0)
      val direction: Vector = hand.direction

      val xPos = normalizeXPos(hand)
      val yPos = direction.pitch
      setRelativePosition(xPos, yPos)
    } else {
      setHandLost
    }
    for (gesture <- frame.gestures().asScala) {
      gesture.`type` match {
        case Gesture.Type.TYPE_KEY_TAP =>
          val keyTap = new KeyTapGesture(gesture)
          clickMouse
        case _ =>
          println("Unknown gesture type.")
      }
    }
  }
}
