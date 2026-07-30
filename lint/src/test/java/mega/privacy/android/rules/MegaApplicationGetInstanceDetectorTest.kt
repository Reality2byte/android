package mega.privacy.android.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class MegaApplicationGetInstanceDetectorTest : BaseLintTest() {

    override fun getDetector(): Detector = MegaApplicationGetInstanceDetector()

    override fun getIssues(): List<Issue> = listOf(MegaApplicationGetInstanceDetector.ISSUE)

    private val megaApplicationStub: TestFile = kotlin(
        "mega/privacy/android/app/MegaApplication.kt",
        """
            package mega.privacy.android.app

            class MegaApplication {
                companion object {
                    private lateinit var instance: MegaApplication
                    fun getInstance(): MegaApplication = instance
                }
            }
        """.trimIndent()
    ).indented().within("src")

    private val otherClassStub: TestFile = kotlin(
        "com/example/SomeOther.kt",
        """
            package com.example

            class SomeOther {
                companion object {
                    fun getInstance(): SomeOther = SomeOther()
                }
            }
        """.trimIndent()
    ).indented().within("src")

    @Test
    fun `test that detector flags qualified MegaApplication getInstance call`() {
        lint().files(
            megaApplicationStub,
            kotlin(
                """
                    package test
                    import mega.privacy.android.app.MegaApplication

                    fun test() {
                        val app = MegaApplication.getInstance()
                    }
                """.trimIndent()
            ).indented().within("src")
        ).run().expectErrorCount(1)
    }

    @Test
    fun `test that detector flags imported bare getInstance call`() {
        lint().files(
            megaApplicationStub,
            kotlin(
                """
                    package test
                    import mega.privacy.android.app.MegaApplication.Companion.getInstance

                    fun test() {
                        val app = getInstance()
                    }
                """.trimIndent()
            ).indented().within("src")
        ).run().expectErrorCount(1)
    }

    @Test
    fun `test that detector does not flag getInstance on another class`() {
        lint().files(
            otherClassStub,
            kotlin(
                """
                    package test
                    import com.example.SomeOther

                    fun test() {
                        val other = SomeOther.getInstance()
                    }
                """.trimIndent()
            ).indented().within("src")
        ).run().expectClean()
    }
}
