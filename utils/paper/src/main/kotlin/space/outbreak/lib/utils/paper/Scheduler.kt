package space.outbreak.lib.utils.paper

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

abstract class SchedulerBase {
    val isFolia = Bukkit.getServer().name.contains("Folia")
    abstract val plugin: JavaPlugin

    val dynamicCommandRegistrationSupported = !isFolia

    fun runNextTick(task: Runnable) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task)
        } else {
            Bukkit.getScheduler().runTask(plugin, task)
        }
    }

    fun runAsync(task: Runnable): Task {
        return if (isFolia) Task(
            Bukkit.getAsyncScheduler().runNow(plugin) { task.run() }
        ) else Task(
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task)
        )
    }

    fun runTimerAsync(task: Runnable, delay: Long, period: Long): Task {
        return if (isFolia)
            Task(
                Bukkit.getAsyncScheduler().runAtFixedRate(
                    plugin,
                    { task.run() },
                    if (delay < 1) 1 else delay, period * 50,
                    TimeUnit.MILLISECONDS
                )
            )
        else
            Task(
                Bukkit.getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    task, delay, period
                )
            )
    }

    fun runLater(task: Runnable, delayTicks: Long): Task {
        return if (isFolia)
            Task(
                Bukkit.getGlobalRegionScheduler().runDelayed(
                    plugin, { task.run() }, delayTicks
                )
            )
        else
            Task(
                Bukkit.getScheduler().runTaskLater(
                    plugin,
                    task, delayTicks
                )
            )
    }

    fun runLaterAsync(task: Runnable, delayTicks: Long): Task {
        return if (isFolia)
            Task(
                Bukkit.getAsyncScheduler().runDelayed(
                    plugin, { task.run() }, delayTicks * 50,
                    TimeUnit.MILLISECONDS
                )
            )
        else
            Task(
                Bukkit.getScheduler().runTaskLaterAsynchronously(
                    plugin,
                    task, delayTicks
                )
            )
    }

    fun runTimer(task: Runnable, delay: Long, period: Long): Task {
        if (task is SchedulerRunnable && !task.isStarted)
            return task.runTimer(delay, period)

        return if (isFolia)
            Task(
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    plugin, { task.run() },
                    if (delay < 1) 1 else delay, period
                )
            )
        else
            Task(
                Bukkit.getScheduler().runTaskTimer(
                    plugin,
                    task, delay, period
                )
            )
    }

    fun executeInGlobalRegion(task: Runnable) {
        if (isFolia)
            Bukkit.getGlobalRegionScheduler().execute(plugin, task)
        else
            task.run()
    }

    abstract inner class SchedulerRunnable : Runnable {
        private var cancelled = false
        private var task: Task? = null
        var isStarted = false
            private set

        val isCancelled get() = task?.isCancelled ?: false

        fun cancel() {
            task?.cancel()
        }

        fun runTimer(delay: Long, period: Long): Task {
            isStarted = true
            val t = this@SchedulerBase.runTimer(this, delay, period)
            task = t
            return t
        }
    }

    class Task private constructor(
        private val foliaTask: ScheduledTask?,
        private val bukkitTask: BukkitTask?
    ) {
        constructor(foliaTask: ScheduledTask) : this(foliaTask, null)
        constructor(bukkitTask: BukkitTask) : this(null, bukkitTask)

        val isCancelled: Boolean
            get() {
                if (foliaTask != null)
                    return foliaTask.isCancelled
                return bukkitTask!!.isCancelled
            }

        fun cancel() {
            if (foliaTask != null)
                foliaTask.cancel()
            else
                bukkitTask!!.cancel()
        }
    }
}
