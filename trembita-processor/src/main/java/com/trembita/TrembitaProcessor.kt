package com.trembita

import com.google.auto.service.AutoService
import com.trembita.annotation.Trembita
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
class TrembitaProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(Trembita::class.java.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val typeElements =
                roundEnv.getElementsAnnotatedWith(Trembita::class.java)
                        .map {
                            buildAnnotatedClass(it)
                        }
        generateTrembitaClass(typeElements)
        return true
    }

    private fun buildAnnotatedClass(typeElement: Element): TrembitaClass {
        val variableNames = typeElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .map { it.simpleName.toString() }

        return TrembitaClass(typeElement, variableNames)
    }

    @Throws(TrembitaException::class, IOException::class)
    private fun generateTrembitaClass(trembitaClasses: List<TrembitaClass>) {
        if (trembitaClasses.isEmpty()) {
            return
        }
        trembitaClasses.forEach {
            val packageName = packageName(processingEnv.elementUtils,
                    it.typeElement)
            val generatedClass = TrembitaGenerator.generateClass(it)
            val javaFile = FileSpec.builder(packageName, generatedClass.name ?: "").addType(generatedClass).build()
            val options = processingEnv.options
            val generatedPath = options["kapt.kotlin.generated"]
            val path = generatedPath
                    ?.replace("(.*)tmp(/kapt/debug/)kotlinGenerated".toRegex(), "$1generated/source$2")
            javaFile.writeTo(File(path, "${javaFile.name}.kt"))
        }
    }

    private fun packageName(elementUtils: Elements, typeElement: Element): String {
        val pkg = elementUtils.getPackageOf(typeElement)
        if (pkg.isUnnamed) {
            throw TrembitaException(typeElement.simpleName.toString())
        }
        return pkg.qualifiedName.toString()
    }

}

