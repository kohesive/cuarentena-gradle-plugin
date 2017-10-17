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

                    // TODO: implement
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