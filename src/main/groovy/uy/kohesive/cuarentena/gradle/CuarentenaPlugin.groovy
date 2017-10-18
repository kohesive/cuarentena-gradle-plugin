package uy.kohesive.cuarentena.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class CuarentenaPlugin implements Plugin<Project> {

    void apply(Project project) {
        def configs = project.container(CuarentenaConfigurationExtension)
        project.extensions.cuarentena = configs

        project.task('generateAllowances') {
            doLast {
                project.cuarentena.each { config ->
                    println "Generating allowances for $config.name (scan mode: $config.scanMode)"

                    // We need to exclude any transient kotlin dependencies first and form a jars list
                    project.configurations.getByName(config.name) {
                        exclude group: 'org.jetbrains.kotlin', module: 'kotlin-stdlib'
                    }
                    def jarsWithoutKotlin = project.configurations.getByName(config.name).collect { file -> file.path }

                    // Then we need to add kotlin stdlib of a user-specified version and form a classpath
                    def kotlinDependencyConfigName = "cuarentenaKotlin-${config.name}"
                    project.configurations.create(kotlinDependencyConfigName)
                    project.dependencies.add(kotlinDependencyConfigName, [group: 'org.jetbrains.kotlin', name: 'kotlin-stdlib', version: config.kotlinVersion])

                    def classPathToVerify = (project.configurations.getByName(config.name) + project.configurations.getByName(kotlinDependencyConfigName)).collect { file ->
                        new URL("file://${file.canonicalFile.absolutePath}")
                    }

                    if (config.verbose) {
                        println 'JARs to be verified:'
                        jarsWithoutKotlin.forEach { x -> println(x.toString()) }
                        println()
                        println 'Verification classpath:'
                        classPathToVerify.forEach { x -> println(x.toString()) }
                    }

                    // We need to get rid of kotlin which is already in gradle's classpath. E.g., kotlin wrapper 4.2.1 contains kotlin 1.1.3
                    def oldClassLoader    = Thread.currentThread().contextClassLoader as URLClassLoader
                    def oldKotlinLessUrls = oldClassLoader.URLs.findAll { url -> !url.toString().contains("kotlin") }
                    def tempClassLoader   = new URLClassLoader((classPathToVerify + oldKotlinLessUrls).toArray() as URL[])

                    def outputDir  = new File(project.getBuildDir(), "generated-src/policy")
                    def outputFile = new File(outputDir, "${config.policyPackage.replace('.', '/')}/${config.name}.ctena")
                    outputFile.parentFile.mkdirs()

                    Thread.currentThread().contextClassLoader = tempClassLoader
                    try {
                        def generatorArgs = [
                            jarsWithoutKotlin,
                            config.scanMode.toString(),
                            config.excludePackages,
                            config.excludeClasses,
                            config.verbose,
                            { String s -> println(s) }
                        ].toArray()

                        // Output policy
                        def generator = tempClassLoader.loadClass("uy.kohesive.cuarentena.kotlin.JarAllowancesGenerator").newInstance(generatorArgs)
                        generator.writePolicy(outputFile.path)
                        println "\nDone writing policy file to ${outputFile.path}"

                        def metaInfDir = new File(outputDir, "META-INF/cuarentena")
                        metaInfDir.mkdirs()
                        def metadataFile = new File(metaInfDir, "${config.name}.properties")
                        metadataFile.write("resource=${config.policyPackage.replace('.', '/')}/${config.name}.ctena\n")
                    }
                    finally {
                        Thread.currentThread().contextClassLoader = oldClassLoader
                    }
                }
            }
        }

        project.task('listConfigs') {
            doLast {
                project.cuarentena.each { config ->
                    println "Curentena config for $config.name (scan mode: $config.scanMode)"

                    // Exclude transitive kotlin dependencies
                    if (config.excludeKotlin) {
                        project.configurations.getByName(config.name) {
                            exclude group: 'org.jetbrains.kotlin'
                        }
                    }

                    // Output exclusions
                    if (!config.excludePackages.isEmpty()) {
                        println ' Excluded packages:'
                        config.excludePackages.forEach { exclude ->
                            println "  - $exclude"
                        }
                    }
                    if (!config.excludeClasses.isEmpty()) {
                        println ' Excluded classes:'
                        config.excludeClasses.forEach { exclude ->
                            println "  - $exclude"
                        }
                    }

                    // Output JARs
                    println ' JARs to be scanned:'
                    project.configurations[config.name].forEach { file ->
                        println "  - $file.path"
                    }
                }
            }
        }
    }

}