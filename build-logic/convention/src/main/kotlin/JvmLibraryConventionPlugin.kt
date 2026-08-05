import mega.privacy.android.gradle.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

/**
 * Conventions for JVM(non-Android) library modules
 *
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    /**
     * Apply this plugin to the given target object.
     *
     * @param target
     */
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("kotlin")
                apply("mega.jvm.test")
                apply("mega.jvm.jacoco")
                apply("mega.lint")
            }
            configureKotlin()
            setJvmToolChainVersion()
        }
    }
}

/**
 * Pins the Java toolchain so that both `compileJava` and `compileKotlin` target the same
 * JVM version regardless of the JDK the Gradle daemon happens to run on.
 */
private fun Project.setJvmToolChainVersion() {
    val jdk: String by rootProject.extra

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(jdk.toInt()))
    }
}
