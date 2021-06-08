package tech.cuda.woden.scheduler.runner

import io.mockk.*
import java.io.File
import java.util.*
import kotlin.io.path.createTempDirectory

object EnvSetter {

    /**
     * 将 Windows 路径转为 WSL 路径，用于解决 WSL 下找不到文件的问题
     */
    fun autoConvertPathFromWindows2WSL(block: () -> Unit) {
        val isWindows = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")
        if (isWindows) {
            // 先生成一个临时文件以及它的 mock
            val tempFile = File.createTempFile("__adhoc__", ".temp")
            val spy = spyk(tempFile)
            // 然后改写 mock 的 absolutePath
            every { spy.absolutePath } returns tempFile.absolutePath.replace("\\", "/").replace("C:/", "/mnt/c/")
            // 最后将 createTempFile 的返回值重定向到 mock
            mockkStatic(File::class)
            every { File.createTempFile("__adhoc__", ".temp") } returns spy
            block()
            unmockkStatic(File::class)
        } else {
            block()
        }
    }

    fun autoSetLocalAndDerbyDir(block: () -> Unit) {
        val tempLocalDir = createTempDirectory(prefix = "__adhoc_spark_Local__").toFile()
        val tempDerbyDir = createTempDirectory(prefix = "__adhoc_derby__").toFile()
        val sparkLocal = tempLocalDir.path.replace("\\", "/")
        val derbyHome = tempDerbyDir.path.replace("\\", "/")

        val log4j = this.javaClass.classLoader.getResource("log4j.properties")!!.path
        val sparkConf = mapOf(
            "spark.local.dir" to sparkLocal,
            "spark.driver.extraJavaOptions" to "-Dlog4j.configuration=file:$log4j",
            "spark.executor.extraJavaOptions" to "-Dlog4j.configuration=file:$log4j",
            "spark.driver.extraJavaOptions" to "-Dderby.system.home=$derbyHome",
            "spark.sql.crossJoin.enabled" to "true"
        )
        mockkConstructor(PySparkRunner::class)
        mockkConstructor(SparkShellRunner::class)
        mockkConstructor(SparkSQLRunner::class)

        every { anyConstructed<PySparkRunner>().sparkConf } returns sparkConf
        every { anyConstructed<SparkShellRunner>().sparkConf } returns sparkConf
        every { anyConstructed<SparkSQLRunner>().sparkConf } returns sparkConf

        block()

        unmockkConstructor(PySparkRunner::class)
        unmockkConstructor(SparkShellRunner::class)
        unmockkConstructor(SparkSQLRunner::class)

        tempDerbyDir.deleteRecursively()
        tempLocalDir.deleteRecursively()
    }

}