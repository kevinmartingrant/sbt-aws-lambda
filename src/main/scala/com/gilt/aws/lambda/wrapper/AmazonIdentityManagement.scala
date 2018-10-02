package com.gilt.aws.lambda.wrapper

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model._
import scala.util.Try

trait AmazonIdentityManagement {
  def listRoles(): Try[ListRolesResult]
  def createRole(req: CreateRoleRequest): Try[CreateRoleResult]
}

object AmazonIdentityManagement {
  def instance(): AmazonIdentityManagement = {
    val auth = new DefaultAWSCredentialsProviderChain()
    val client = AmazonIdentityManagementClientBuilder.standard()
        .withCredentials(auth)
        .build

    new AmazonIdentityManagement {
        def listRoles() = Try(client.listRoles)
        def createRole(req: CreateRoleRequest) = Try(client.createRole(req))
    }
  }
}
