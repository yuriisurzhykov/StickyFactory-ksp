package com.yuriisurzhykov.stickyevents.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

/*
 * Provides helper functions and collections for handling symbol processing with Kotlin KSP.
 */

/**
 * Retrieves symbols(classes) annotated with a given annotation.
 *
 * @param cls The KClass of the annotation to search for.
 * @return A sequence of KSClassDeclarations annotated with the specified annotation.
 */
internal fun Resolver.getSymbols(cls: KClass<*>) =
    this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
        .filterIsInstance<KSClassDeclaration>()
        .filter(KSNode::validate)

/**
 * Checks if the collection of modifiers contains a specific name.
 *
 * @param name The name of the modifier to check for.
 * @return True if any modifier in the collection contains the specified name.
 */
internal fun Collection<Modifier>.contains(name: String): Boolean {
    return any { it.name.contains(name) }
}


/*
 * Maps of various collection class names to their corresponding string builders.
 */
internal val ListClassNames = mapOf(
    "kotlin.collections.MutableList" to TypeStringBuilder.CollectionBuilder("mutableListOf()"),
    "kotlin.collections.ArrayList" to TypeStringBuilder.CollectionBuilder(),
    "kotlin.collections.List" to TypeStringBuilder.CollectionBuilder("listOf()"),
    "java.util.List" to TypeStringBuilder.CollectionBuilder("listOf()"),
    "java.util.ArrayList" to TypeStringBuilder.CollectionBuilder(),
)

internal val SetClassNames = mapOf(
    "kotlin.collections.MutableSet" to TypeStringBuilder.CollectionBuilder("mutableSetOf()"),
    "kotlin.collections.Set" to TypeStringBuilder.CollectionBuilder("setOf()"),
    "java.util.Set" to TypeStringBuilder.CollectionBuilder("setOf()"),
    "kotlin.collections.HashSet" to TypeStringBuilder.CollectionBuilder(),
    "java.util.HashSet" to TypeStringBuilder.CollectionBuilder(),
    "kotlin.collections.LinkedHashSet" to TypeStringBuilder.CollectionBuilder(),
    "java.util.LinkedHashSet" to TypeStringBuilder.CollectionBuilder(),
    "java.util.NavigableSet" to TypeStringBuilder.CollectionBuilder(),
    "java.util.SortedSet" to TypeStringBuilder.CollectionBuilder()
)

internal val DequeClassNames = mapOf(
    "kotlin.collections.ArrayDeque" to TypeStringBuilder.CollectionBuilder(),
    "java.util.ArrayDeque" to TypeStringBuilder.CollectionBuilder(),
    "java.util.Deque" to TypeStringBuilder.CollectionBuilder("java.util.ArrayDeque()")
)

internal val MapClassNames = mapOf(
    "kotlin.collections.MutableMap" to TypeStringBuilder.CollectionBuilder("mutableMapOf()"),
    "kotlin.collections.Map" to TypeStringBuilder.CollectionBuilder("mapOf()"),
    "java.util.Map" to TypeStringBuilder.CollectionBuilder("mutableMapOf()"),
    "kotlin.collections.HashMap" to TypeStringBuilder.CollectionBuilder(),
    "java.util.HashMap" to TypeStringBuilder.CollectionBuilder(),
    "kotlin.collections.LinkedHashMap" to TypeStringBuilder.CollectionBuilder(),
    "java.util.LinkedHashMap" to TypeStringBuilder.CollectionBuilder(),
    "java.util.SortedMap" to TypeStringBuilder.CollectionBuilder("java.util.TreeMap()"),
    "java.util.NavigableMap" to TypeStringBuilder.CollectionBuilder("java.util.TreeMap()"),
    "java.util.TreeMap" to TypeStringBuilder.CollectionBuilder(),
    "java.util.concurrent.ConcurrentMap" to TypeStringBuilder.CollectionBuilder("java.util.concurrent.ConcurrentHashMap()"),
    "java.util.concurrent.ConcurrentHashMap" to TypeStringBuilder.CollectionBuilder()
)

internal val MapOfCollections =
    (MapClassNames + SetClassNames + ListClassNames + DequeClassNames)

internal val MapOfUnsignedTypes = mapOf(
    "kotlin.UByte" to TypeStringBuilder.UnsignedBuilder(),
    "kotlin.UShort" to TypeStringBuilder.UnsignedBuilder(),
    "kotlin.UInt" to TypeStringBuilder.UnsignedBuilder(),
    "kotlin.ULong" to TypeStringBuilder.UnsignedBuilder("L")
)

internal val MapOfArrays = mapOf(
    "kotlin.Array" to TypeStringBuilder.ArrayBuilder(),
    "kotlin.BooleanArray" to TypeStringBuilder.ArrayBuilder("boolean"),
    "kotlin.ByteArray" to TypeStringBuilder.ArrayBuilder("byte"),
    "kotlin.CharArray" to TypeStringBuilder.ArrayBuilder("char"),
    "kotlin.DoubleArray" to TypeStringBuilder.ArrayBuilder("double"),
    "kotlin.FloatArray" to TypeStringBuilder.ArrayBuilder("float"),
    "kotlin.IntArray" to TypeStringBuilder.ArrayBuilder("int"),
    "kotlin.LongArray" to TypeStringBuilder.ArrayBuilder("long"),
    "kotlin.ShortArray" to TypeStringBuilder.ArrayBuilder("short"),
)

internal val MapOfBasicTypes = mapOf(
    "kotlin.Boolean" to TypeStringBuilder.BooleanBuilder(),
    "kotlin.Byte" to TypeStringBuilder.ByteBuilder(),
    "kotlin.Short" to TypeStringBuilder.ShortBuilder(),
    "kotlin.Int" to TypeStringBuilder.IntBuilder(),
    "kotlin.Long" to TypeStringBuilder.LongBuilder(),
    "kotlin.Float" to TypeStringBuilder.FloatBuilder(),
    "kotlin.Double" to TypeStringBuilder.DoubleBuilder(),
    "kotlin.Char" to TypeStringBuilder.CharBuilder(),
    "kotlin.String" to TypeStringBuilder.StringBuilder(),
    "java.lang.String" to TypeStringBuilder.StringBuilder(),
) + MapOfUnsignedTypes + MapOfArrays
