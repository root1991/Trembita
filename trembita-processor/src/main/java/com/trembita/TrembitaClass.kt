package com.trembita

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

internal class TrembitaClass(val typeElement: Element, val variableNames: List<String>) {

    val type: TypeMirror
        get() = typeElement.asType()
}
