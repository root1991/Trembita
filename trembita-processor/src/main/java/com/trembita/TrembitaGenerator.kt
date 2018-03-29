package com.trembita

import com.squareup.kotlinpoet.*
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror


internal object TrembitaGenerator {

    fun generateClass(trembitaClass: TrembitaClass): TypeSpec {
        val className = trembitaClass.type.toString().split(".").last()
        val builder = TypeSpec.classBuilder("Trembita" + className)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addSuperinterface(trembitaClass.type.asTypeName())
        generateTrembitaMethods(builder, trembitaClass)
        return builder.build()
    }

    private fun generateTrembitaMethods(classBuilder: TypeSpec.Builder, trembitaClass: TrembitaClass) {
        trembitaClass.typeElement.enclosedElements.forEach {
            when (it.kind) {
                ElementKind.METHOD -> {
                    it as ExecutableElement

                    val propertyReceiver = PropertySpec.builder("_" + it.simpleName,
                            LambdaTypeName.get(null,
                                    processLambdaArgs(it.parameters), Unit::class.asTypeName())
                                    .asNullable(), KModifier.PRIVATE)
                            .mutable(true)
                            .initializer("null")
                    val methodBuilder = FunSpec.builder(it.simpleName.toString())
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameters(processParameters((it.asType() as ExecutableType).parameterTypes))
                            .addStatement("_" + it.simpleName.toString() + "?.invoke(" +
                                    getInvokedParams((it.asType() as ExecutableType).parameterTypes) + ")")
                    val initMethodBuilder = FunSpec.builder(it.simpleName.toString())
                            .returns(Unit::class)
                            .addParameter(ParameterSpec.builder("receiver", LambdaTypeName.get(null,
                                    processLambdaArgs(it.parameters), Unit::class.asTypeName()))
                                    .build())
                            .addStatement("_" + it.simpleName.toString() + " = receiver")

                    classBuilder.apply {
                        addProperty(propertyReceiver.build())
                        addFunction(methodBuilder.build())
                        addFunction(initMethodBuilder.build())
                    }
                }
                else -> throw IllegalStateException("Fields are not supported")
            }
        }
    }

    private fun convertToKClass(javaName: TypeName) =
            with(javaName.toString()) {
                if (contains("java")) {
                    when {
                        equals("java.lang.String") -> String::class.asTypeName()
                        equals("java.lang.Double") -> Double::class.asTypeName()
                        contains("HashMap") -> processParametrisedType(javaName, HashMap::class.asClassName())
                        contains("kotlin.Array") -> processParametrisedType(javaName, ARRAY)
                        contains("java.util.List") -> processParametrisedType(javaName, List::class.asClassName())
                        contains("java.util.ArrayList") -> processParametrisedType(javaName, ArrayList::class.asClassName())
                        equals("java.lang.Integer") -> Int::class.asTypeName()
                        equals("java.lang.Float") -> Float::class.asTypeName()
                        contains("Any") -> Any::class.asTypeName()
                        equals("java.lang.Byte") -> Byte::class.asTypeName()
                        contains("Char") -> Char::class.asTypeName()
                        contains("DeprecationLevel") -> DeprecationLevel::class.asTypeName()
                        equals("java.lang.Long") -> Long::class.asTypeName()
                        equals("java.lang.Short") -> Short::class.asTypeName()
                        contains("Unit") -> Unit::class.asTypeName()
                        else -> javaName
                    }
                } else {
                    javaName
                }
            }

    private fun processLambdaArgs(types: List<VariableElement>) = types.map {
        convertToKClass(it.asType().asTypeName())
    }

    private fun processParametrisedType(javaName: TypeName, className: ClassName): ParameterizedTypeName {
        val validTypes = (javaName as ParameterizedTypeName).typeArguments.map {
            convertToKClass(it)
        }
        return ParameterizedTypeName.get(className, *validTypes.toTypedArray())
    }


    private fun getInvokedParams(types: List<TypeMirror>): String {
        val invokers = StringBuilder()
        types.mapIndexed { index, _ ->
            invokers.append("arg" + index)
            if (index + 1 != types.size) invokers.append(",")
        }
        return invokers.toString()
    }

    private fun processParameters(types: List<TypeMirror>) =
            types.mapIndexed { index: Int, it: TypeMirror ->
                ParameterSpec.builder("arg" + index, convertToKClass(it.asTypeName())).build()
            }

}
