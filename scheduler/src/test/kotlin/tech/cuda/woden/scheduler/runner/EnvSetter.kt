package tech.cuda.woden.scheduler.runner

import io.mockk.*
import java.io.File

object EnvSetter {

    /**
     * 将 Windows 路径转为 WSL 路径，用于解决 WSL 下找不到文件的问题
     */
    fun autoConvertPathFromWindows2WSL(block: () -> Unit) {
        val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
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
        val tempLocalDir = createTempDir(prefix = "__adhoc__", suffix = ".spark.local")
        val tempDerbyDir = createTempDir(prefix = "__adhoc__", suffix = ".derby")
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
        mockkConstructor(PySparkAdhoc::class)
        mockkConstructor(SparkShellAdhoc::class)
        mockkConstructor(SparkSQLAdhoc::class)

        every { anyConstructed<PySparkAdhoc>().sparkConf } returns sparkConf
        every { anyConstructed<SparkShellAdhoc>().sparkConf } returns sparkConf
        every { anyConstructed<SparkSQLAdhoc>().sparkConf } returns sparkConf

        block()

        unmockkConstructor(PySparkAdhoc::class)
        unmockkConstructor(SparkShellAdhoc::class)
        unmockkConstructor(SparkSQLAdhoc::class)

        tempDerbyDir.deleteRecursively()
        tempLocalDir.deleteRecursively()
    }

}