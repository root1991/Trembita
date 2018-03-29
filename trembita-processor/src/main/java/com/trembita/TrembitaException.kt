package com.trembita

internal class TrembitaException(className: String)
    : Exception("The package of $className class has no name")
