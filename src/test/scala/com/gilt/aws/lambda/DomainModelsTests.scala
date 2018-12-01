package com.gilt.aws.lambda

import utest._

object DomainModelsTests extends TestSuite {
  val tests = Tests {
    "Timeout" - {
      "above 0" - timeoutAbove0
      "equal, or bellow 900" - timeoutEqualBellow900
    }

    "Memory" - {
      "dividable by 64" - memoryDividable64
      "equal, or above 128" - memoryEqualAbove128
      "equal, or bellow 1536" - memoryEqualBellow1536
    }
  }

  def timeoutAbove0 = {
    intercept[IllegalArgumentException]{ Timeout(-1) }
    intercept[IllegalArgumentException]{ Timeout(0) }
    Timeout(1)
  }

  def timeoutEqualBellow900 = {
    Timeout(899)
    Timeout(900)
    intercept[IllegalArgumentException]{ Timeout(901) }
  }

  def memoryDividable64 = {
    intercept[IllegalArgumentException]{ Memory(255) }
    Memory(256)
    intercept[IllegalArgumentException]{ Memory(257) }
  }

  def memoryEqualAbove128 = {
    intercept[IllegalArgumentException]{ Memory(64) }
    Memory(128)
    Memory(256)
  }

  def memoryEqualBellow1536 = {
    Memory(1472)
    Memory(1536)
    intercept[IllegalArgumentException]{ Memory(1600) }
  }
}
