package com.gilt.aws.lambda

import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.{CreateRoleRequest, Role}

import scala.util.{Failure, Success, Try}

private[lambda] object AwsIAM {

  val BasicLambdaRoleName = "lambda_basic_execution"

  lazy val iamClient = AmazonIdentityManagementClientBuilder.standard().withCredentials(AwsCredentials.provider).build

  def basicLambdaRole(): Option[Role] = {
    import scala.collection.JavaConverters._
    val existingRoles = iamClient.listRoles().getRoles.asScala

    existingRoles.find(_.getRoleName == BasicLambdaRoleName)
  }

  def createBasicLambdaRole(): Try[RoleARN] = {
    val createRoleRequest = {
      val policyDocument = """{"Version":"2012-10-17","Statement":[{"Sid":"","Effect":"Allow","Principal":{"Service":"lambda.amazonaws.com"},"Action":"sts:AssumeRole"}]}"""
      new CreateRoleRequest()
        .withRoleName(BasicLambdaRoleName)
        .withAssumeRolePolicyDocument(policyDocument)
    }

    try {
      val result = iamClient.createRole(createRoleRequest)
      Success(RoleARN(result.getRole.getArn))
    } catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        Failure(ex)
    }
  }
}
