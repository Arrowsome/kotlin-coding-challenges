package com.igorwojda.puzzletest

import com.igorwojda.puzzletest.utils.KotlinParserUtils
import com.igorwojda.puzzletest.utils.PuzzleFile
import com.igorwojda.puzzletest.utils.TestUtils
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.opentest4j.AssertionFailedError
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

class ProjectConsistencyTest {

    @ParameterizedTest(name = "Puzzle file exists: {0}")
    @MethodSource("getPuzzleRequiredFilePaths")
    fun `Puzzle file exists`(puzzleFilePath: String) {
        val path = Path(puzzleFilePath)
        require(Files.exists(path)) { "Missing file $path" }
    }

    @ParameterizedTest(name = "Verify challenge kt file top level items: {0}")
    @MethodSource("getSolutionFiles")
    fun `solution kt file has solution objects`(ktFile: KtFile) {
        // given
        val solutions = ktFile.children.filterIsInstance<KtObjectDeclaration>()

        // then
        solutions.size shouldBeGreaterOrEqualTo 1
    }

    @ParameterizedTest(name = "Verify challenge kt file top level items: {0}")
    @MethodSource("getSolutionFiles")
    fun `solution kt file has solution objects with correct names`(ktFile: KtFile) {
        // given
        val solutionNames = ktFile.children.filterIsInstance<KtObjectDeclaration>().map { it.name ?: ""}

        // then
        solutionNames.forEach {
            if (!it.startsWith("Solution")) {
               throw AssertionFailedError("Solution object name does not contains 'Solution' prefix $it")
            }
        }
    }

    @ParameterizedTest(name = "Verify challenge kt file top level items: {0}")
    @MethodSource("getChallenge")
    fun `challenge kt file has one top level function`(ktFile: KtFile) {
        // given
        val functions = ktFile.children.filterIsInstance<KtNamedFunction>()

        // then
        functions.size shouldBeEqualTo 1
    }

    @ParameterizedTest(name = "Verify challenge kt file top level items: {0}")
    @MethodSource("getChallenge")
    fun `challenge kt file has one top level Test class`(ktFile: KtFile) {
        // given
        val classes = ktFile.children.filterIsInstance<KtClass>().filter { it.name == "Test" }

        // then
        classes.size shouldBeEqualTo 1
    }

    companion object {
        @JvmStatic
        fun getSolutionFiles() = TestUtils
            .getPuzzleDirectories()
            .map { KotlinParserUtils.getKtFile(it, PuzzleFile.SOLUTIONS_KT) }

        @JvmStatic
        fun getChallenge() = TestUtils
            .getPuzzleDirectories()
            .map { KotlinParserUtils.getKtFile(it, PuzzleFile.CHALLENGE_KT) }

        @JvmStatic
        fun getPuzzleRequiredFilePaths() = TestUtils
            .getPuzzleDirectories()
            .flatMap {
                getProjectRequiredFiles(it)
            }

        private fun getProjectRequiredFiles(puzzleDirectory: File) = PuzzleFile
            .values()
            .map {
                "${puzzleDirectory.path}/${it.fileName}"
            }
    }
}
