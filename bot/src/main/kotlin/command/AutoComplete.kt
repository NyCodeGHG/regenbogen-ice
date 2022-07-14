package dev.nycode.regenbogenice.command

const val autoCompleteCode = """
    if (autoCompleteCallback == null) {
        autoComplete { with(converter) { onAutoComplete() } }      
    }
"""
