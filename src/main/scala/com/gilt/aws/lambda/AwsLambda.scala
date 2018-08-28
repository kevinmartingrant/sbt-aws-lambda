package com.gilt.aws.lambda

import java.io.RandomAccessFile
import java.nio.ByteBuffer

import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model._
import sbt._

import scala.util.{Failure, Success, Try}

private[lambda] object AwsLambda {

  def updateLambdaWithFunctionCodeRequest(region: Region,
                                          updateFunctionCodeRequest: UpdateFunctionCodeRequest): Try[UpdateFunctionCodeResult] = {
    try {
      val client = AWSLambdaClientBuilder.standard()
        .withCredentials(AwsCredentials.provider)
        .withRegion(region.value)
        .build

      val updateResult = client.updateFunctionCode(updateFunctionCodeRequest)

      println(s"Updated lambda code ${updateResult.getFunctionArn}")
      Success(updateResult)
    }
    catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        Failure(ex)
    }
  }

  def getLambdaConfig(region: Region,
                      functionName: LambdaName): Try[Option[GetFunctionConfigurationResult]] = Try {
    val request = new GetFunctionConfigurationRequest().withFunctionName(functionName.value)

    val client = AWSLambdaClientBuilder.standard()
      .withCredentials(AwsCredentials.provider)
      .withRegion(region.value)
      .build

    Some(client.getFunctionConfiguration(request))
  } recover {
    case _: ResourceNotFoundException => None
  }

  def updateLambdaConfig(region: Region,
                         functionName: LambdaName,
                         handlerName: HandlerName,
                         roleName: RoleARN,
                         timeout:  Option[Timeout],
                         memory: Option[Memory],
                         deadLetterName: Option[DeadLetterARN],
                         vpcConfig: Option[VpcConfig],
                         environment: Environment): Try[UpdateFunctionConfigurationResult] = Try {
    var request = new UpdateFunctionConfigurationRequest()
        .withFunctionName(functionName.value)
        .withHandler(handlerName.value)
        .withRole(roleName.value)
        .withRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)
        .withEnvironment(environment)
    request = timeout.fold(request)(t => request.withTimeout(t.value))
    request = memory.fold(request)(m => request.withMemorySize(m.value))
    request = vpcConfig.fold(request)(request.withVpcConfig)
    request = deadLetterName.fold(request)(d => request.withDeadLetterConfig(new DeadLetterConfig().withTargetArn(d.value)))

    val client = AWSLambdaClientBuilder.standard()
      .withCredentials(AwsCredentials.provider)
      .withRegion(region.value)
      .build

    val updateResult = client.updateFunctionConfiguration(request)

    println(s"Updated lambda config ${updateResult.getFunctionArn}")
    updateResult
  }

  def createLambda(region: Region,
                   functionName: LambdaName,
                   handlerName: HandlerName,
                   roleName: RoleARN,
                   timeout:  Option[Timeout],
                   memory: Option[Memory],
                   deadLetterName: Option[DeadLetterARN],
                   vpcConfig: Option[VpcConfig],
                   functionCode: Option[FunctionCode],
                   environment: Environment
                    ): Try[CreateFunctionResult] = {
    try {
      val client = AWSLambdaClientBuilder.standard().withCredentials(AwsCredentials.provider).withRegion(region.value).build()

      var request = new CreateFunctionRequest()
        .withFunctionName(functionName.value)
        .withHandler(handlerName.value)
        .withRole(roleName.value)
        .withRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)
        .withEnvironment(environment)
      request = timeout.fold(request)(t => request.withTimeout(t.value))
      request = memory.fold(request)(m => request.withMemorySize(m.value))
      request = vpcConfig.fold(request)(request.withVpcConfig)
      request = deadLetterName.fold(request)(n => request.withDeadLetterConfig(new DeadLetterConfig().withTargetArn(n.value)))
      request = functionCode.fold(request)(request.withCode)

      val createResult = client.createFunction(request)

      println(s"Created Lambda: ${createResult.getFunctionArn}")
      Success(createResult)
    }
    catch {
      case ex@(_: AmazonClientException |
               _: AmazonServiceException) =>
        Failure(ex)
    }
  }

  def getJarBuffer(jar: File): ByteBuffer = {
    val buffer = ByteBuffer.allocate(jar.length().toInt)
    val aFile = new RandomAccessFile(jar, "r")
    val inChannel = aFile.getChannel()
    while (inChannel.read(buffer) > 0) {}
    inChannel.close()
    buffer.rewind()
    buffer
  }
}
