package knowledgeeditorapp


import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(L2LEditorInterceptor)
class L2LEditorInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test l2LEditor interceptor matching"() {
        when: "A request matches the interceptor"
        withRequest(controller: "l2LEditor")

        then: "The interceptor does match"
        interceptor.doesMatch()
    }
}
