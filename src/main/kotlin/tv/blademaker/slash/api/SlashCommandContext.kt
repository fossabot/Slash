package tv.blademaker.slash.api

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import tv.blademaker.slash.api.actions.ContextAction
import java.util.concurrent.atomic.AtomicReference

@Suppress("unused")
interface SlashCommandContext {

    val commandClient: SlashCommandClient
    val event: SlashCommandEvent

    val isAcknowledged: Boolean
        get() = event.isAcknowledged

    val jda: JDA
        get() = event.jda

    @Suppress("MemberVisibilityCanBePrivate")
    val hook: InteractionHook
        get() = event.hook

    val options: List<OptionMapping>
        get() = event.options

    val guild: Guild
        get() = event.guild!!

    val member: Member
        get() = event.member!!

    val selfMember: Member
        get() = event.guild!!.selfMember

    val channel: TextChannel
        get() = event.channel as TextChannel

    val author: User
        get() = event.user

    fun acknowledge(ephemeral: Boolean = false): ReplyAction {
        if (isAcknowledged) throw IllegalStateException("Current command is already ack.")
        return event.deferReply(ephemeral)
    }

    fun getOption(name: String) = event.getOption(name)

    // Replies

    /**
     * Reply to the event with the given content.
     *
     * @param content The content for the message.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyMessage(content: String) = ContextAction.build(this, content).reply()

    /**
     * Reply to the event with the given content.
     *
     * @param message The message to reply with.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyMessage(message: Message) = ContextAction.build(this, message).reply()

    /**
     * Reply to the event with the given content.
     *
     * @param builder The message builder.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyMessage(builder: MessageBuilder) = ContextAction.build(this, builder).reply()

    /**
     * SReply to the event with the given content.
     *
     * @param builder The message builder function.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyMessage(builder: MessageBuilder.() -> Unit) = ContextAction.build(this, builder).reply()

    // Embed actions

    /**
     * Reply to the event with the given embed.
     *
     * @param embed The embed.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyEmbed(embed: MessageEmbed) = ContextAction.build(this, embed).reply()

    /**
     * Reply to the event with the given embed.
     *
     * @param builder The embed builder.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyEmbed(builder: EmbedBuilder) = ContextAction.build(this, builder).reply()

    /**
     * Reply to the event with the given embed.
     *
     * @param builder The embed builder function.
     *
     * @return A [WebhookMessageAction]
     */
    fun replyEmbed(builder: EmbedBuilder.() -> Unit) = ContextAction.build(this, builder).reply()

    // Followup messages

    /**
     * Send a follow-up message with the given content.
     *
     * @param content The content for the message.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendMessage(content: String) = ContextAction.build(this, content).send()

    /**
     * Send a follow-up message with the given content.
     *
     * @param message The message to send.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendMessage(message: Message) = ContextAction.build(this, message).send()

    /**
     * Send a follow-up message with the given content.
     *
     * @param builder The message builder.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendMessage(builder: MessageBuilder) = ContextAction.build(this, builder).send()

    /**
     * Send a follow-up message with the given content.
     *
     * @param builder The message builder function.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendMessage(builder: MessageBuilder.() -> Unit) = ContextAction.build(this, builder).send()

    // Embed actions

    /**
     * Send a follow-up message with the given embed.
     *
     * @param embed The embed.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendEmbed(embed: MessageEmbed) = ContextAction.build(this, embed).send()

    /**
     * Send a follow-up message with the given embed.
     *
     * @param builder The embed builder.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendEmbed(builder: EmbedBuilder) = ContextAction.build(this, builder).send()

    /**
     * Send a follow-up message with the given embed.
     *
     * @param builder The embed builder function.
     *
     * @return A [WebhookMessageAction]
     */
    fun sendEmbed(builder: EmbedBuilder.() -> Unit) = ContextAction.build(this, builder).send()

    // DSL Builders

    /**
     * Build a [ContextAction] using DSL.
     *
     * @param builder An embed builder function.
     *
     * @return The context action for the embed.
     */
    fun embed(builder: EmbedBuilder.() -> Unit) = ContextAction.build(this, builder)

    /**
     * Build a [ContextAction] using DSL.
     *
     * @param builder An message builder function.
     *
     * @return The context action for the message.
     */
    fun message(builder: MessageBuilder.() -> Unit) = ContextAction.build(this, builder)

    /**
     * An extra object (reference) the set what you want.
     */
    var extra: AtomicReference<Any?>
}