package com.thilko.springdoc.model

import groovy.json.JsonOutput
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import javax.lang.model.type.TypeKind

class Method {

    private methodElement

    def cssClasses = ["GET": "label label-primary",
            "POST": "label label-success",
            "PUT": "label label-info",
            "DELETE": "label label-danger"]

    static def fromElement(methodElement) {
        new Method(methodElement)
    }

    private Method(methodElement) {
        this.methodElement = methodElement
    }

    def name() {
        this.methodElement.simpleName
    }

    def httpMethod() {
        !hasRequestMethod() ? "GET" : requestMappingAnnotation().method().first().name()
    }

    private boolean hasRequestMethod() {
        requestMappingAnnotation().method().length > 0
    }

    private requestMappingAnnotation() {
        (RequestMapping) methodElement.getAnnotation(RequestMapping.class)
    }

    def httpMethodCssClass() {
        this.cssClasses[httpMethod()]
    }

    def path() {
        def value = requestMappingAnnotation().value()
        value.length == 0 ? "/" : value.first()
    }

    def queryParameter() {
        methodElement.parameters
                .findAll { it.getAnnotation(RequestParam.class) != null }
                .collect { QueryParameter.fromVariableElement(it) }
    }

    def url() {
        def base = "http://example.com${path()}"
        if (!queryParameter().isEmpty()) {
            def parameter = queryParameter().collect { "${it.name()}=" }.join("&")
            base += "?${parameter}"
        }
        return base
    }

    def queryParameter(name) {
        queryParameter().find { it.name() == name }
    }

    def response() {
        Response.fromReturnType(methodElement.returnType)
    }

    def hasResponse(){
        methodElement.returnType.kind != TypeKind.VOID
    }

    def hasRequestBody(){
        methodElement.parameters.find { it.getAnnotation(RequestBody.class) != null } != null
    }

    def requestBody() {
        def bodyArg = methodElement.parameters.find { it.getAnnotation(RequestBody.class) != null }
        if(bodyArg == null){
            return ""
        }

        def domainClass = this.class.classLoader.loadClass(bodyArg.asType().toString())
        JsonOutput.prettyPrint(new JsonOutput().toJson(domainClass.newInstance()))
    }
}
