package dev.nycode.regenbogenice.command

import com.kotlindiscord.kord.extensions.commands.converters.Validator
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.nycode.regenbogenice.train.autocomplete
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import org.koin.core.component.inject

abstract class TrainAutoCompletingArgument<T : Any>(override var validator: Validator<T>) :
    AutoCompletingArgument<T>(validator) {

    private val rainbow by inject<RainbowICE>()

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        val trains = autocomplete(focusedOption.value)
        suggestString {
            for (train in trains.take(25)) {
                trainChoice(train)
            }
        }
    }
}
