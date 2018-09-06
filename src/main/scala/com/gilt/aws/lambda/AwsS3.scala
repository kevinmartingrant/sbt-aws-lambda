package com.gilt.aws.lambda

import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{Bucket, CannedAccessControlList, PutObjectRequest}
import sbt._

import scala.util.{Failure, Success, Try}

private[lambda] object AwsS3 {
  def pushJarToS3(region: Region, jar: File, bucketId: S3BucketId, s3KeyPrefix: String): Try[S3Key] = {
    try {
      val client = AmazonS3ClientBuilder.standard().withCredentials(AwsCredentials.provider).withRegion(region.value).build()
      val key = s3KeyPrefix + jar.getName
      val objectRequest = new PutObjectRequest(bucketId.value, key, jar)
        .withCannedAcl(CannedAccessControlList.AuthenticatedRead)

      client.putObject(objectRequest)

      Success(S3Key(key))
    } catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        Failure(ex)
    }
  }

  def getBucket(region: Region, bucketId: S3BucketId): Option[Bucket] = {
    import scala.collection.JavaConverters._
    try {
      val client = AmazonS3ClientBuilder.standard().withCredentials(AwsCredentials.provider).withRegion(region.value).build()
      client.listBuckets().asScala.find(_.getName == bucketId.value)
    } catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        None
    }
  }

  def createBucket(region: Region, bucketId: S3BucketId): Try[S3BucketId] = {
    try {
      val client = AmazonS3ClientBuilder.standard().withCredentials(AwsCredentials.provider).withRegion(region.value).build()
      client.createBucket(bucketId.value)
      Success(bucketId)
    } catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        Failure(ex)
    }
  }
}
