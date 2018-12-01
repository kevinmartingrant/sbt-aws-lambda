package com.gilt.aws.lambda

import java.time.Instant

import software.amazon.awssdk.services.lambda.model._
// import com.amazonaws.services.lambda.model._
import scala.collection.JavaConverters._
import scala.util.Try

private[lambda] class AwsLambda(client: wrapper.AwsLambda) {

  def updateLambdaWithFunctionCodeRequest(updateFunctionCodeRequest: UpdateFunctionCodeRequest): Try[UpdateFunctionCodeResponse] = {
    println(s"Updating lambda code ${updateFunctionCodeRequest.functionName}")
    client.updateFunctionCode(updateFunctionCodeRequest)
      .map { result =>
        println(s"Updated lambda code ${result.functionArn}")
        result
      }
  }

  def tagLambda(functionArn: String, version: String) = {
    val tags = Map(
      "deploy.code.version" -> version,
      "deploy.timestamp" -> Instant.now.toString
    )

    val tagResourceReq = new TagResourceRequest()
      .withResource(functionArn)
      .withTags(tags.asJava)

    client.tagResource(tagResourceReq)
  }

  def getLambdaConfig(functionName: LambdaName): Try[Option[GetFunctionConfigurationResponse]] = {
    val request = new GetFunctionConfigurationRequest()
      .functionName(functionName.value)

    client.getFunctionConfiguration(request)
      .map(Option.apply)
      .recover {
        case _: ResourceNotFoundException => None
      }
  }

  def updateLambdaConfig(functionName: LambdaName,
                         handlerName: HandlerName,
                         roleName: RoleARN,
                         timeout:  Option[Timeout],
                         memory: Option[Memory],
                         deadLetterName: Option[DeadLetterARN],
                         vpcConfig: Option[VpcConfig],
                         environment: Environment): Try[UpdateFunctionConfigurationResponse] = {

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

    client.updateFunctionConfiguration(request)
      .map { result =>
        println(s"Updated lambda config ${result.getFunctionArn}")
        result
      }
  }

  def createLambda(functionName: LambdaName,
                   handlerName: HandlerName,
                   roleName: RoleARN,
                   timeout:  Option[Timeout],
                   memory: Option[Memory],
                   deadLetterName: Option[DeadLetterARN],
                   vpcConfig: Option[VpcConfig],
                   functionCode: FunctionCode,
                   environment: Environment): Try[CreateFunctionResponse] = {

    var request = new CreateFunctionRequest()
      .withFunctionName(functionName.value)
      .withHandler(handlerName.value)
      .withRole(roleName.value)
      .withRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)
      .withEnvironment(environment)
      .withCode(functionCode)
    request = timeout.fold(request)(t => request.withTimeout(t.value))
    request = memory.fold(request)(m => request.withMemorySize(m.value))
    request = vpcConfig.fold(request)(request.withVpcConfig)
    request = deadLetterName.fold(request)(n => request.withDeadLetterConfig(new DeadLetterConfig().withTargetArn(n.value)))

    client.createFunction(request)
      .map { result =>
        println(s"Created Lambda: ${result.functionArn}")
        result
      }

  }
}
