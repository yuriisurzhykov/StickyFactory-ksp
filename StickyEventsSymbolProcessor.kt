package com.yuriisurzhykov.stickyevents.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.niceforyou.events.Event
import com.niceforyou.events.FactoryComponent
import com.niceforyou.events.StickyComponent
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

/**
 * A symbol processor for generating factory implementations for sticky events
 * using Kotlin Symbol Processing (KSP).
 *
 * This processor handles annotations related to sticky events and generates
 * corresponding factory class at compile time.
 */
class StickyEventsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /**
     * Logs an error message when an error occurs during processing.
     */
    override fun onError() {
        logger.error("error happened")
    }

    /**
     * Processes symbols (classes) annotated with specific annotations to generate
     * factory implementations.
     *
     * @param resolver The Resolver for accessing type information.
     * @return An empty list, as this processor does not produce additional symbols to be processed.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val stickyElements = resolver.getSymbols(StickyComponent::class).toList()
        val factoryInterface = resolver.getSymbols(FactoryComponent::class).toList()
        if (factoryInterface.isNotEmpty()) {
            val factoryDescription = generateFactory(factoryInterface.first(), stickyElements)
            logger.info("factory structure generated: $factoryDescription.")
            factoryDescription.writeTo(
                codeGenerator,
                Dependencies.ALL_FILES
            )
        }
        return emptyList()
    }

    /**
     * Generates a KotlinPoet FileSpec representing the factory class
     * for the specified factory and sticky events.
     *
     * @param factory The KSClassDeclaration representing the factory interface.
     * @param events The list of KSClassDeclarations representing sticky events.
     * @return A FileSpec object representing the generated factory class.
     */
    private fun generateFactory(
        factory: KSClassDeclaration,
        events: List<KSClassDeclaration>
    ): FileSpec {
        val packageName = factory.packageName.asString()
        logger.info("factory package: $packageName")
        val factoryName = factory.toClassName().simpleName + "Impl"
        logger.info("factory name: $factoryName")
        return FileSpec.builder(packageName, factoryName)
            .addType(
                TypeSpec
                    .classBuilder(factoryName)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"")
                            .build()
                    )
                    .addSuperinterface(factory.toClassName())
                    .addFunction(
                        FunSpec
                            .constructorBuilder()
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("produce")
                            .addModifiers(KModifier.OVERRIDE)
                            .addTypeVariable(
                                TypeVariableName(
                                    "T",
                                    Event.Sticky::class.asTypeName()
                                )
                            )
                            .addParameter(
                                "kClass",
                                KClass::class.asTypeName().parameterizedBy(TypeVariableName("T"))
                            )
                            .returns(TypeVariableName("T"))
                            .beginControlFlow("return when(kClass)")
                            .apply {
                                events.forEach { event ->
                                    addStatement(
                                        "${event.toClassName().canonicalName}::class -> ${
                                            buildConstructorCall(event)
                                        } as T"
                                    )
                                }
                                addStatement(
                                    """else -> throw IllegalStateException(
                                        |"${'$'}kClass doesn't match any of declared sticky events!" +
                                        |"If your ${'$'}kClass inherited from Event.Sticky you also " +
                                        |"have to mark it with @StickyComponent annotation")""".trimMargin()
                                )
                            }
                            .endControlFlow()
                            .build()
                    )
                    .build()
            )
            .build()
    }

    /**
     * Builds a constructor call for future factory file for a given class type.
     *
     * @param classType The KSClassDeclaration of the class for which to build a constructor call.
     * @return A string representing the constructor call.
     */
    private fun buildConstructorCall(classType: KSClassDeclaration): String {
        return TypeStringBuilder.CustomTypeBuilder(logger, false).build(classType)
    }
}
