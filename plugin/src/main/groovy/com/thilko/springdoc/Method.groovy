package com.thilko.springdoc

import org.springframework.web.bind.annotation.RequestMapping

class Method {

    def name
    def httpMethod

    static def fromElement(executable) {
        new Method(executable)
    }

    private Method(executable) {
        def requestAnnotation = (RequestMapping) executable.getAnnotation(RequestMapping.class)
        this.name = executable.simpleName
        this.httpMethod = requestAnnotation.method().length == 0 ? "GET" : requestAnnotation.method().first().name()
    }

}
