package org.labun.jooq.generator.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.labun.jooq.generator.DefaultGenerator
import org.labun.jooq.generator.Defaults
import org.labun.jooq.generator.GenerationTool
import org.labun.jooq.generator.config.Configuration
import org.labun.jooq.generator.config.DatabaseConfig
import org.labun.jooq.generator.config.SubGeneratorConfig

/**
 * @author Konstantin Labun
 */
class JooqCodeGenerationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def configuration = new Configuration()
        configuration.database = new DatabaseConfig()
        configuration.database.schemas = ["PUBLIC"]

        JooqCodeGenerationExtension extension = project.extensions.create("jooqCodeGeneration", JooqCodeGenerationExtension.class)

        project.task('generateJOOQ') {
            doLast {
                configuration.database.with(extension.database)

                List<SubGeneratorConfig> defaultConfigs = new Defaults.CodeGenerationConfigs().all()

                def sourceRootConfigurer = {
                    it.generatedSourcesRoot = "${project.getBuildDir()}/generated/src/main/java/"
                }

                def subGenerators = new ArrayList<>(configuration.subGenerators)
                extension.generators.each({ cfg ->
                    def generator = new SubGeneratorConfig()
                    generator.with(sourceRootConfigurer)
                    generator.with(cfg)

                    def defaultGenerator = defaultConfigs.find({
                        it.generatorName == generator.generatorName
                    })
                    if (defaultGenerator != null) {
                        generator = defaultGenerator
                        generator.with(sourceRootConfigurer)
                        generator.with(cfg)
                    }

                     subGenerators.add(generator)
                })

                configuration.subGenerators = subGenerators
                new GenerationTool().generate(new DefaultGenerator(configuration))
            }
        }
    }
}