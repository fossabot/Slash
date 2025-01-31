package tv.blademaker.slash.client

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.sharding.ShardManager
import tv.blademaker.slash.context.ContextCreator
import tv.blademaker.slash.context.impl.ContextCreatorImpl
import tv.blademaker.slash.exceptions.ExceptionHandlerImpl
import tv.blademaker.slash.exceptions.ExceptionHandler
import tv.blademaker.slash.internal.CommandExecutionCheck
import tv.blademaker.slash.internal.SuspendingCommandExecutor
import tv.blademaker.slash.metrics.MetricsStrategy
import tv.blademaker.slash.ratelimit.RateLimit
import tv.blademaker.slash.ratelimit.RateLimitHandler

class DefaultSlashCommandBuilder(
    private val packageName: String
) {
    private var metrics: MetricsStrategy? = null

    private var contextCreator: ContextCreator? = null

    private var exceptionHandler: ExceptionHandler? = null

    private val checks = mutableSetOf<CommandExecutionCheck>()

    private var rateLimitConfiguration: RateLimitHandler.Configuration = RateLimitHandler.Configuration()

    fun enableMetrics(): DefaultSlashCommandBuilder {
        this.metrics = MetricsStrategy()
        return this
    }

    fun enableMetrics(builder: MetricsStrategy.() -> Unit): DefaultSlashCommandBuilder {
        this.metrics = MetricsStrategy().apply(builder)
        return this
    }

    fun contextCreator(contextCreator: ContextCreator): DefaultSlashCommandBuilder {
        this.contextCreator = contextCreator
        return this
    }

    fun addCheck(check: CommandExecutionCheck): DefaultSlashCommandBuilder {
        if (checks.contains(check)) error("check already registered.")
        checks.add(check)
        return this
    }

    fun configureRateLimit(configuration: RateLimitHandler.Configuration.() -> Unit): DefaultSlashCommandBuilder {
        rateLimitConfiguration = RateLimitHandler.Configuration().apply(configuration)
        return this
    }

    private fun build(): DefaultSlashCommandClient {
        return DefaultSlashCommandClient(
            packageName,
            exceptionHandler ?: ExceptionHandlerImpl(),
            contextCreator ?: ContextCreatorImpl(),
            checks,
            rateLimitConfiguration,
            metrics
        )
    }

    fun buildWith(jda: JDA): DefaultSlashCommandClient {
        val client = build()

        jda.addEventListener(client)

        return client
    }

    fun buildWith(shardManager: ShardManager): DefaultSlashCommandClient {
        val client = build()

        shardManager.addEventListener(client)

        return client
    }

}