package tv.blademaker.slash.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import tv.blademaker.slash.annotations.InteractionTarget
import tv.blademaker.slash.client.DefaultSlashCommandClient
import tv.blademaker.slash.context.AutoCompleteContext
import tv.blademaker.slash.context.GuildSlashCommandContext
import tv.blademaker.slash.context.SlashCommandContext
import tv.blademaker.slash.extensions.newCoroutineDispatcher
import kotlin.coroutines.CoroutineContext

open class SuspendingCommandExecutor(
    private val client: DefaultSlashCommandClient
) : CoroutineScope {

    private val dispatcher = newCoroutineDispatcher("slash-commands-worker-%s", 2, 50)

    override val coroutineContext: CoroutineContext
        get() = dispatcher + Job()

    private suspend fun creteContext(handler: SlashCommandHandler, event: SlashCommandInteractionEvent): SlashCommandContext {
        return when (handler.target) {
            InteractionTarget.GUILD -> client.contextCreator.createGuildContext(event)
            else -> client.contextCreator.createContext(event)
        }
    }

    private suspend fun checkGlobals(ctx: SlashCommandContext): Boolean {
        if (client.checks.isEmpty()) return true
        return client.checks.all { it(ctx) }
    }

    internal fun execute(event: SlashCommandInteractionEvent, handler: SlashCommandHandler) = launch {
        try {
            client.metrics?.incHandledCommand(event)
            val ctx = creteContext(handler, event)

            if (!checkGlobals(ctx)) return@launch

            if (!handler.parent.doChecks(ctx)) return@launch
            if (ctx is GuildSlashCommandContext) Checks.handlerPermissions(ctx, handler.permissions)

            val startTime = System.nanoTime()
            handler.execute(ctx)
            val time = (System.nanoTime() - startTime) / 1_000_000

            client.metrics?.incSuccessCommand(event, time)
        } catch (e: Exception) {
            client.exceptionHandler.wrap(e, handler.parent, event)
            client.metrics?.incFailedCommand(event)
        }
    }

    internal fun execute(event: CommandAutoCompleteInteractionEvent, handler: AutoCompleteHandler) = launch {
        try {
            val ctx = AutoCompleteContext(event)

            handler.execute(ctx)
        } catch (e: Exception) {
            client.exceptionHandler.wrap(e, handler.parent, event)
        }
    }
}