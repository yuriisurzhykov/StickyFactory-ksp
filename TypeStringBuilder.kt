package com.yuriisurzhykov.stickyevents.ksp

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Interface defining a mechanism for building default value strings for various data types.
 * Used in the generation of code for default parameter values in constructors.
 */
interface TypeStringBuilder {

    /**
     * Builds a default value string for a given type.
     *
     * @param valueParam The declaration representing the type.
     * @return A string representation of the default value for the specified type.
     */
    fun build(valueParam: KSDeclaration): String

    /**
     * Implementation of [TypeStringBuilder] for generating default Boolean values.
     */
    class BooleanBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Boolean") "false"
            else throw IllegalArgumentException("Class is not Boolean")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Byte values.
     */
    class ByteBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Byte") "0"
            else throw IllegalArgumentException("Class is not Byte")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Char values.
     */
    class CharBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Char") "'0'"
            else throw IllegalArgumentException("Class is not Byte")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Integer values.
     */
    class IntBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Int") "0"
            else throw IllegalArgumentException("Class is not Integer")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Float values.
     */
    class FloatBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Float") "0.0f"
            else throw IllegalArgumentException("Class is not Float")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Double values.
     */
    class DoubleBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Float") "0.0"
            else throw IllegalArgumentException("Class is not Double")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Long values.
     */
    class LongBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Long") "0L"
            else throw IllegalArgumentException("Class is not Double")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default Short values.
     */
    class ShortBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.Short") "0"
            else throw IllegalArgumentException("Class is not Double")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default values for any of unsigned types.
     */
    class UnsignedBuilder(
        private val suffix: String? = null
    ) : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className in MapOfUnsignedTypes.keys) {
                if (suffix == null) "0u" else "0u$suffix"
            } else throw IllegalArgumentException("Class is not Double")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default String values.
     */
    class StringBuilder : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className == "kotlin.String" || className == "java.lang.String") "\"\""
            else throw IllegalArgumentException("Class is not String")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default any of array values.
     */
    class ArrayBuilder(
        private val prefix: String? = null
    ) : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className in MapOfArrays.keys) {
                if (prefix == null) "arrayOf()" else "${prefix}ArrayOf()"
            } else throw IllegalArgumentException("Class is not an Array")
        }
    }

    /**
     * Implementation of [TypeStringBuilder] for generating default collection values.
     */
    class CollectionBuilder(
        private val invocationStatement: String? = null
    ) : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val className = valueParam.qualifiedName?.asString().toString()
            return if (className in MapOfCollections.keys) {
                invocationStatement ?: "$className()"
            } else throw IllegalArgumentException("Class is not a Map")
        }
    }

    /**
     *  Implementation of [TypeStringBuilder] for generating custom enum types values.
     *  By default it returns first value of enum list.
     */
    class EnumBuilder(
        private val nullable: Boolean
    ) : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val declaration = valueParam.closestClassDeclaration()

            if (declaration?.classKind == ClassKind.ENUM_CLASS) {
                // Obtaining all enum values
                val enumValues = declaration.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .map { it.qualifiedName?.asString().toString() }

                // Return first value from enum list or null if list is empty
                return enumValues.firstOrNull() ?: if (nullable) "null"
                else throw IllegalArgumentException(
                    "Not able to provide default value for class: " +
                            declaration.simpleName.asString()
                )
            } else {
                throw IllegalArgumentException("Class is not an Enum")
            }
        }
    }

    /**
     *  Implementation of [TypeStringBuilder] for generating custom enum types values.
     *  By default it returns first value of enum list.
     */
    class ObjectTypeBuilder(
        private val nullable: Boolean
    ) : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            val declaration = valueParam.closestClassDeclaration()

            if (declaration?.classKind == ClassKind.OBJECT) {
                // Return first value from enum list or null if list is empty
                return declaration.qualifiedName?.asString() ?: if (nullable) "null"
                else throw IllegalArgumentException(
                    "Not able to provide default value for class: " +
                            declaration.simpleName.asString()
                )
            } else {
                throw IllegalArgumentException("Class is not an Enum")
            }
        }
    }

    /**
     * Implementation for building default values for custom types.
     * It analyzes constructors and generates default parameter values accordingly.
     *
     * @param logger The KSPLogger for logging information.
     * @param valueNullable Indicates whether the value can be nullable.
     */
    class CustomTypeBuilder(
        private val logger: KSPLogger,
        private val valueNullable: Boolean
    ) : TypeStringBuilder {
        override fun build(valueParam: KSDeclaration): String {
            // Try to find class declaration for the given symbol
            val declaration = valueParam.closestClassDeclaration()
            if (declaration != null) {
                // Define string builder for constructor invocation string
                val constructorStringBuilder = java.lang.StringBuilder()
                constructorStringBuilder.append(declaration.qualifiedName?.asString().toString())
                    .append("(")

                // Try to find constructor without parameters
                val emptyConstructor =
                    declaration.getConstructors().find { it.parameters.isEmpty() }

                if (emptyConstructor == null) {
                    logger.info("No empty constructor for ${declaration.qualifiedName?.asString()}")

                    // Try to find constructor without custom data types.
                    val constructorsWithoutCustomTypes =
                        declaration.getConstructors().filter(::filterPrimitives)

                    val primaryConstructor = declaration.primaryConstructor
                    if (constructorsWithoutCustomTypes.count() >= 1) {
                        logger.info("Using constructor without custom data types for ${declaration.qualifiedName?.asString()}")

                        // If there is constructor without custom data types then just add type
                        // string representation.
                        val params = constructorsWithoutCustomTypes.first().parameters
                        val stringParams =
                            params.joinToString(", ", transform = ::transformPrimitiveToString)

                        constructorStringBuilder.append(stringParams)
                    } else if (primaryConstructor != null) {
                        // Receive all parameter for primary constructor
                        val params = primaryConstructor.parameters

                        logger.info("Using primary constructor: ${primaryConstructor.qualifiedName?.asString()}")

                        // Mapping every data type to its corresponding allocation string
                        val stringParams = params.joinToString(", ") { param ->
                            val type = param.type.resolve()
                            val builder = when (val typeName =
                                type.declaration.qualifiedName?.asString().toString()) {
                                in MapOfBasicTypes.keys -> MapOfBasicTypes[typeName]

                                in MapOfCollections.keys -> MapOfCollections[typeName]

                                else -> {
                                    val typeDeclaration = type.declaration.closestClassDeclaration()
                                    when (typeDeclaration?.classKind) {
                                        ClassKind.ENUM_CLASS -> EnumBuilder(type.isMarkedNullable)
                                        ClassKind.OBJECT -> ObjectTypeBuilder(type.isMarkedNullable)
                                        else -> CustomTypeBuilder(logger, type.isMarkedNullable)
                                    }
                                }
                            }
                            builder?.build(type.declaration).toString()
                        }
                        constructorStringBuilder.append(stringParams)
                    }
                }
                // Closing constructor parentheses and returning constructor invocation string
                return constructorStringBuilder.append(")").toString()
            } else if (valueNullable) {
                // If no class definition found and parameter marked as null then passing null
                logger.warn("No declaration found for ${valueParam.qualifiedName?.asString()}. Using 'null' for it")
                return "null"
            } else {
                // If no definition and param not nullable throwing error
                logger.error(
                    "Not possible to create default parameters for" +
                            "class ${valueParam.simpleName}"
                )
                return ""
            }
        }

        private fun filterPrimitives(constructor: KSFunctionDeclaration): Boolean {
            return constructor.parameters.all { param ->
                val className =
                    param.type.resolve().declaration.qualifiedName?.asString()
                        .toString()
                className in MapOfBasicTypes.keys
            }
        }

        private fun transformPrimitiveToString(param: KSValueParameter): CharSequence {
            val type = param.type.resolve()
            val declaration = type.declaration

            if (type.isMarkedNullable) return "null"

            return MapOfBasicTypes[declaration.qualifiedName?.asString().toString()]
                ?.build(declaration).toString()
        }
    }
}
