package com.thilko.springdoc

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class SpringAnnotationProcessorTest extends Specification {
    def compiler
    def indexHtmlFile

    def setup() {
        compiler = TestCompiler.javaCompiler()
        indexHtmlFile = Paths.get("index.html")
    }

    def cleanup() {
        compiler.cleanup()
    }

    def "parser can be initialized with classes"() {
        when:
        compiler.compile()

        then:
        !compiler.hasErrors()
        Files.exists(indexHtmlFile)
    }

    def "html doc contains resources"() {
        when:
        compiler.compile()

        then:
        !compiler.hasErrors()
        indexHtml().contains("/customer")
    }

    def "html doc contains root path if exist"() {
        when:
        compiler.compile()

        then:
        !compiler.hasErrors()
        indexHtml().contains("/statistics")
    }

    def "html doc contains the operations"() {
        when:
        compiler.compile()

        then:
        !compiler.hasErrors()
        indexHtml().contains("updateCustomer")
        indexHtml().contains("getCompletedInvoices")
    }

    def "html doc does not contain the constructor method"() {
        when:
        compiler.compile()

        then:
        !indexHtml().contains("&ltinit&gt")
    }

    def "html doc contains the HTTP method of operations"() {
        when:
        compiler.compile()

        then:
        indexHtml().contains("GET")
    }

    def "html doc contains the HTTP parameters of operations"() {
        when:
        startCompilation()

        then:
        indexHtml().contains("customerid")
    }

    def "html doc contains an example url"() {
        when:
        startCompilation()

        then:
        indexHtml().contains("http://example.com/customers/invoices/completed?amount=&amp;customerid=")
    }

    private static def indexHtml() {
        new File("index.html").getText()
    }

    private void startCompilation() {
        compiler.compile()
    }
}
