package com.gilt.aws.lambda

import com.amazonaws.services.identitymanagement.model._
import scala.util.{Failure, Success, Try}

import utest._

trait NotImplementedAmazonIdentityManagementWrapper extends wrapper.AmazonIdentityManagement {
  def listRoles(): Try[ListRolesResult] = ???
  def createRole(req: CreateRoleRequest): Try[CreateRoleResult] = ???
}

object AwsIAMTests extends TestSuite {
  val tests = Tests {
    "Get basic lambda role" - {
      "gets role if name match" - getSome
      "gets none if no name match" - getNoneNoMatch
      "gets none if failure" - getNoneFailure
    }
    "Create basic lambda role" - {
      "creates with name" - createWithName
      "creates return role arn" - createReturnsArn
    }
  }

  def getSome = {
    val client = new NotImplementedAmazonIdentityManagementWrapper {
      override def listRoles() = {
        val result = (new ListRolesResult).withRoles(
          new Role().withRoleName("a"),
          new Role().withRoleName("b"),
          new Role().withRoleName("c"),
          new Role().withRoleName(AwsIAM.BasicLambdaRoleName)
        )

        Success(result)
      }
    }

    val result = new AwsIAM(client).basicLambdaRole()
    assert(result.nonEmpty)
  }

  def getNoneNoMatch = {
    val client = new NotImplementedAmazonIdentityManagementWrapper {
      override def listRoles() = {
        val result = (new ListRolesResult).withRoles(
          new Role().withRoleName("a"),
          new Role().withRoleName("b"),
          new Role().withRoleName("c")
        )

        Success(result)
      }
    }

    val result = new AwsIAM(client).basicLambdaRole()
    assert(result.isEmpty)
  }

  def getNoneFailure = {
    val client = new NotImplementedAmazonIdentityManagementWrapper {
      override def listRoles() = {
        Failure(new Throwable)
      }
    }

    val result = new AwsIAM(client).basicLambdaRole()
    assert(result.isEmpty)
  }

  def createWithName = {
    val client = new NotImplementedAmazonIdentityManagementWrapper {
      override def createRole(req: CreateRoleRequest) = {
        assert(req.getRoleName() == AwsIAM.BasicLambdaRoleName)
        Failure(new Throwable)
      }
    }

    new AwsIAM(client).createBasicLambdaRole()
  }

  def createReturnsArn = {
    val arn = "my-role-arn"
    val client = new NotImplementedAmazonIdentityManagementWrapper {
      override def createRole(req: CreateRoleRequest) = {
        val role = new Role().withArn(arn)
        val result = new CreateRoleResult().withRole(role)
        Success(result)
      }
    }

    val result = new AwsIAM(client).createBasicLambdaRole()
    assert(result.isSuccess)
    assert(result.get == RoleARN(arn))
  }
}
