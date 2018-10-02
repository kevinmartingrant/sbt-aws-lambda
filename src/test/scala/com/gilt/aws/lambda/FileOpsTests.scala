package com.gilt.aws.lambda

import java.io.File
import java.nio.charset.Charset
import scala.io.Source


import utest._

object FileOpsTests extends TestSuite {
  val tests = Tests {
    "File to Buffer" - {
      "identical content" - identicalContent
    }
  }

  def identicalContent = {
    val file = new File(getClass.getResource("/lipsum.com.txt").getFile)
    val fileContent = Source.fromFile(file, "UTF-8").getLines.mkString("\n")

    val buffer = FileOps.fileToBuffer(file)
    val bufferContent = new String(buffer.array(), Charset.forName("UTF-8"))

    assert(fileContent == bufferContent.trim)
  }

}
