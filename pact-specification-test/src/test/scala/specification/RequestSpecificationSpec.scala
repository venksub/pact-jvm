package specification

import java.io.File

import au.com.dius.pact.model.{FullRequestMatch, RequestMatching, Response, Interaction}
import org.json4s._
import org.json4s.jackson.JsonMethods._

abstract class RequestSpecificationSpec extends SpecificationSpec {

  def test(input: PactRequestSpecification) = {
    val fakeInteraction = Interaction("", None, input.expected, Response(200, Map[String, String](), "", null))
    val result = RequestMatching.compareRequest(fakeInteraction, input.actual)
    if(input.`match`) {
      result mustEqual FullRequestMatch(fakeInteraction)
    } else {
      result mustNotEqual FullRequestMatch(fakeInteraction)
    }
  }

  def fragments(path: String) = {
    val resources = getClass.getResource(path)
    val file = new File(resources.toURI)
    file.listFiles().flatMap { folder =>
      if(folder.isDirectory) {
        val dirName = folder.getName
        folder.listFiles(jsonFilter).map { testFile =>
          val fileName = testFile.getName
          implicit val formats = DefaultFormats
          val testJson = parse(testFile)
          val testData = testJson.transformField {
            case ("body", value) => ("body", JString(pretty(value)))
          }.extract[PactRequestSpecification]

          val description = s"$dirName/$fileName ${testData.comment}"
          fragmentFactory.example(description, {
            test(testData)
          })
        }
      } else {
        Seq()
      }
    }
  }

}
