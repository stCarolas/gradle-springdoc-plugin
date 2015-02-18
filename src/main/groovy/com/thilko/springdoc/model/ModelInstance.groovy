package com.thilko.springdoc.model

import groovy.json.JsonOutput
import org.codehaus.groovy.reflection.ClassInfo

import java.lang.ref.SoftReference
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Set;


class ModelInstance {
    def instance
    private ModelInstanceType[] defaultValues = [
        [accepts: { it == String.class }, value: { "String" }],
        [accepts: { it == Long.class }, value: { 42L }],
        [accepts: { it == long.class }, value: { 42L }],
        [accepts: { it == Integer.class }, value: { 41 }],
        [accepts: { it == int.class }, value: { 41 }],
        [accepts: { it == Double.class }, value: { 77.7d }],
        [accepts: { it == double.class }, value: { 77.7d }],
        [accepts: { it == BigInteger.class }, value: { BigInteger.ONE }],
        [accepts: { it == BigDecimal.class }, value: { BigDecimal.TEN }],
        [accepts: { it == Date.class }, value: { new Date(0) }],
        [accepts: { it == boolean.class }, value: { true }],
        [accepts: { it == Boolean.class }, value: { true }],
    ] as ModelInstanceType[]

    private ModelInstanceType[] fieldsToIgnore = [
        [accepts: { it.type == ClassInfo.class }],
        [accepts: { it.type == MetaClass.class }],
        [accepts: { it.type == SoftReference.class }],
        [accepts: { it.type == Class.class }],
        [accepts: { it.type == Set.class }],
        [accepts: { it.name == "serialVersionUID"}],
        [accepts: { it.name.startsWith("_") }],
        [accepts: { Modifier.isFinal(it.modifiers)}]
    ] as ModelInstanceType[]

    static def fromClass(Class<?> aClass) {
        ModelInstance instance = new ModelInstance()
        if (aClass.is(List)) {
            instance.instance = new ArrayList<>()
        } else {
            instance.instance = aClass.newInstance()
        }

        return instance
    }

    def toJson() {
        fillInstance(instance)
        JsonOutput.prettyPrint(new JsonOutput().toJson(instance))
    }

    def fillInstance(Object instance) {
        instance.class.declaredFields.toList().findAll { fieldToFilter ->
            !fieldsToIgnore.any { it.accepts(fieldToFilter) }
        }.each { field ->
            boolean wasApplied = false
            field.setAccessible(true)
            defaultValues.each {
                if (it.accepts(field.type)) {
                    field.set(instance, it.value())
                    wasApplied = true
                }
            }

            if (!wasApplied) {
                if (field.type.is(List)) {
                    ParameterizedType t = (ParameterizedType) field.getGenericType();

                    def clazz = Class.forName(((Class) t.actualTypeArguments[0]).name)
                    def defaultValue = defaultValues.find { it.accepts(clazz) }
                    if (!defaultValue) {
                        def newInstance = clazz.newInstance()
                        fillInstance(newInstance)
                        field.set(instance, Arrays.asList(newInstance))
                    } else {
                        field.set(instance, Arrays.asList(defaultValue.value()))
                    }
                } else {
                    def newInstance = field.type.newInstance()

                    fillInstance(newInstance)
                    field.set(instance, newInstance);
                }
            }
        }
    }
}
