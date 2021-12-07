package tv.blademaker.slash.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.exceptions.PermissionsLackException
import tv.blademaker.slash.internal.SlashUtils
import tv.blademaker.slash.internal.SlashUtils.toHuman
import tv.blademaker.slash.internal.newCoroutineDispatcher
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

open class DefaultSlashCommandClient(packageName: String) : SlashCommandClient, CoroutineScope {

    private val dispatcher = newCoroutineDispatcher("slash-commands-worker-%s", 2, 50)

    override val coroutineContext: CoroutineContext
        get() = dispatcher + Job()

    override val registry = SlashUtils.discoverSlashCommands(packageName)

    override fun onSlashCommandEvent(event: SlashCommandEvent) {
        launch { handleSuspend(event) }
    }

    open suspend fun createContext(event: SlashCommandEvent, command: BaseSlashCommand): SlashCommandContext {
        return SlashCommandContextImpl(event)
    }

    open fun onPermissionsLackException(ex: PermissionsLackException) {
        when(ex.target) {
            PermissionTarget.BOT -> {
                val perms = ex.permissions.toHuman()
                ex.context.reply("\uD83D\uDEAB The bot does not have the necessary permissions to carry out this action." +
                        "\nRequired permissions: **${perms}**.")
            }
            PermissionTarget.USER -> {
                val perms = ex.permissions.toHuman()
                ex.context.reply("\uD83D\uDEAB You do not have the necessary permissions to carry out this action." +
                        "\nRequired permissions: **${perms}**.")
            }
        }.setEphemeral(true).queue()
    }

    private suspend fun handleSuspend(event: SlashCommandEvent) {
        if (!event.isFromGuild)
            return event.reply("This command is not supported outside a guild.").queue()

        val command = getCommand(event.name) ?: return
        val context = createContext(event, command)

        logCommand(context.guild, "${event.user.asTag} uses command \u001B[33m${event.commandString}\u001B[0m")

        try {
            command.execute(context)
            Metrics.incSuccessCommand(event)
        } catch (e: PermissionsLackException){
            onPermissionsLackException(e)
            Metrics.incFailedCommand(event)
        } catch (e: Exception) {
            SlashUtils.captureSlashCommandException(context, e, logger)
            Metrics.incFailedCommand(event)
        } finally {
            Metrics.incHandledCommand(event)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSlashCommandClient::class.java)

        private fun logCommand(guild: Guild, content: String) = logger.info("[\u001b[32m${guild.name}(${guild.id})\u001b[0m] $content")
    }
}