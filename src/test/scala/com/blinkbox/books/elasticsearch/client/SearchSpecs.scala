package com.blinkbox.books.elasticsearch.client

import com.blinkbox.books.elasticsearch.client.SprayElasticClientRequests._
import com.blinkbox.books.test.FailHelper
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.elasticsearch.index.VersionType
import org.scalatest.{FlatSpec, Matchers}
import spray.http.StatusCodes
import spray.httpx.Json4sJacksonSupport

class SearchSpecs extends FlatSpec with Matchers with ElasticTest {

  import TestFixtures._
  import JsonSupport.json4sUnmarshaller

  override def beforeAll() {
    super.beforeAll()
    successfulRequest(indexDef) check isOk
    successfulRequest(RefreshAllIndices) check isOk
  }

  "The ES HTTP client" should "return an empty result set when searching an empty index" in {
    successfulRequest((search in "catalogue" -> "book" query matchall).sourceIs[Book]) check { resp =>
      resp.hits.total should equal(0)
      resp.hits.hits should be(empty)
    }
  }

  it should "allow performing a query and retrieving some results" in {
    successfulRequest(index into "catalogue" -> "book" id troutBook.isbn doc troutBookSource) check isOk
    successfulRequest(RefreshAllIndices) check isOk
    successfulRequest((search in "catalogue" -> "book" query matchall).sourceIs[Book]) check { resp =>
      resp.hits.total should equal(1)
      resp.hits.hits should contain theSameElementsAs SearchHit("catalogue", "book", troutBook.isbn, troutBook) :: Nil
    }
  }
}