#!/usr/bin/env kotlin

import java.io.File

val maxDepth = 15
val ignore = setOf(".git", "build", ".idea", "out", "gradle")
val root = File(".").canonicalFile
val markdown = false

fun File.ktLines(): Int {
    if (!isFile || extension != "kt") return 0
    var inBlock = false
    return readLines().count { raw ->
        var line = raw.trim()
        if (line.startsWith("import ")) return@count false
        if (inBlock) {
            line.indexOf("*/").takeIf { it >= 0 }?.let {
                line = line.substring(it + 2).trim(); inBlock = false
            } ?: return@count false
        }
        while (true) {
            val b = line.indexOf("/*")
            val c = line.indexOf("//")
            when {
                b >= 0 && (c == -1 || b < c) -> {
                    val e = line.indexOf("*/", b + 2)
                    line = if (e >= 0) (line.substring(0, b) + line.substring(e + 2)).trim()
                    else { inBlock = true; line.substring(0, b).trim() }
                }
                c >= 0 -> { line = line.substring(0, c).trim(); break }
                else -> break
            }
        }
        line.isNotEmpty()
    }
}

fun File.containsKt(depth: Int = 0): Boolean =
    isDirectory && depth <= maxDepth && listFiles()?.any {
        it.extension == "kt" || it.containsKt(depth + 1)
    } == true

fun File.collapse(): Pair<String, File> {
    var f = this
    val names = mutableListOf(name)
    while (true) {
        val dirs = f.listFiles()?.filter { it.isDirectory && it.name !in ignore } ?: break
        if (dirs.size != 1 || f.listFiles()?.any { it.isFile && it.extension == "kt" } == true) break
        f = dirs.first().also { names += it.name }
    }
    return names.joinToString("/") to f
}

fun File.declarations(): List<Pair<Int, String>> {
    if (!isFile || extension != "kt") return emptyList()
    val lines = readLines()
    val result = mutableListOf<Pair<Int, String>>()
    val enumRe = Regex("""\benum\s+class\s+(\w+)""")
    val classRe = Regex("""\b(class|object)\s+(\w+)""")
    val funRe = Regex("""fun\s+(\w+)\s*\(""")
    var depth = 0
    var composable = false

    for (lineRaw in lines) {
        val line = lineRaw.trim()
        if (line.startsWith("@Composable")) {
            composable = true; continue
        }
        enumRe.find(line)?.let { result += depth to "enum class ${it.groupValues[1]}" }
        classRe.find(line)?.let { result += depth to "${it.groupValues[1]} ${it.groupValues[2]}" }
        if (composable) {
            funRe.find(line)?.let { result += depth to "@Composable fun ${it.groupValues[1]}" }
            composable = false
        }
        depth += line.count { it == '{' } - line.count { it == '}' }
        if (depth < 0) depth = 0
    }
    return result
}

fun countLines(f: File): Int =
    if (f.isFile) f.ktLines()
    else f.listFiles()?.sumOf {
        if ((it.isDirectory && it.containsKt()) || (it.isFile && it.extension == "kt")) countLines(it) else 0
    } ?: 0

fun File.shouldSkip(depth: Int) =
    depth > maxDepth || !exists() || name in ignore || (isDirectory && !containsKt())

fun printTree(f: File, indent: String = "", depth: Int = 0): Int {
    if (f.shouldSkip(depth)) return 0
    return if (f.isDirectory) {
        val (path, dir) = f.collapse()
        val children = dir.listFiles()?.filter {
            it.extension == "kt" || it.containsKt()
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()

        val total = children.sumOf { countLines(it) }
        println(if (markdown) "$indent- 📂 `$path` (**$total** lines)" else "$indent[$path] ($total lines)")
        children.forEach { printTree(it, "$indent    ", depth + 1) }
        total
    } else {
        val lines = f.ktLines()
        println(if (markdown) "$indent- 📄 `${f.name}` (**$lines** lines)" else "$indent${f.name} ($lines lines)")
        f.declarations().forEach { (lvl, d) ->
            val prefix = "$indent    ${"  ".repeat(lvl)}"
            println(if (markdown) "$prefix- `$d`" else "$prefix- $d")
        }
        lines
    }
}

println(if (markdown) "### 📦 Kotlin Structure of `${root.name}`" else "Kotlin Structure of: ${root.name}\n")
val total = printTree(root)
println(if (markdown) "\n**Total Kotlin lines:** `$total`" else "\nTotal Kotlin code lines: $total")
