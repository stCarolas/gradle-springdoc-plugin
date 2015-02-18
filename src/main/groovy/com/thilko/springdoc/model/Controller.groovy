package com.thilko.springdoc.model

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import javax.lang.model.element.TypeElement

class Controller {

    private TypeElement resource

    private List<Resource> methods = []

    private Controller(def resource) {
        this.resource = resource
    }

    static def resourceGroupsFor(List<TypeElement> controllerAnnotations) {
        println("origin: " + controllerAnnotations)
        def withControllers = controllerAnnotations.collect{
            createController(it)
        }
        println("with controller: " + withControllers)
        def allMethodsNonFlatten = withControllers.collect { it.methods }
        println("collected methods " + allMethodsNonFlatten)
        def allMethods = allMethodsNonFlatten.flatten()
        println("flattened:  " + allMethods)

        def grouped = allMethods.groupBy { it.baseName() }
        println("grouped: " + grouped)
        grouped.collect {
            new ResourceGroup(resources: it.value, name: it.key.toString())
        }
    }

    static def resourceGroupsFor(TypeElement controllerAnnotation) {
        resourceGroupsFor([controllerAnnotation] as List)
    }

    private static def createController(TypeElement resource) {
        def doc = new Controller(resource)
        resource.accept(new ControllerVisitor(), doc)

        return doc
    }

    def applyExecutable(def executable) {
        if (!isConstructor(executable) && isApiMethod(executable)) {
            this.methods << createApiMethod(executable);
        }
    }

    def requestMappingAnnotation() {
        resource.getAnnotation(RequestMapping)
    }

    private static isApiMethod(def executable) {
        executable.getAnnotation(RequestMapping) != null
    }

    private def createApiMethod(executable) {
        def resource = Resource.fromElement(executable)
//        println("annos: " + resource.requestMappingAnnotation())
//        if (resource.requestMappingAnnotation().method().size() > 1) {
//            resource.requestMappingAnnotation().method().each{RequestMethod method -> 
//                println("method: " + method)
//            }
//        }
        if (hasRequestMapping() && !hasRootPath()) {
            resource.applyPathPrefix(controllerPath())
        }

        resource
    }

    private boolean hasRootPath() {
        return requestMappingAnnotation().value().first() == "/"
    }

    private def controllerPath() {
        requestMappingAnnotation().value().first()
    }

    private def hasRequestMapping() {
        requestMappingAnnotation() != null
    }

    private static def isConstructor(executable) {
        executable.simpleName.contentEquals("<init>")
    }
}
