package com.uet.nvmnghia.yacv.parser.helper

import androidx.documentfile.provider.DocumentFile
import java.io.IOException
import java.util.*


/**
 * A port of [FileTreeWalk] for a tree [DocumentFile].
 */
class DocumentTreeWalk private constructor(
    private val start: DocumentFile,
    private val direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
    private val onEnter: ((DocumentFile) -> Boolean)?,
    private val onLeave: ((DocumentFile) -> Unit)?,
    private val onFail: ((df: DocumentFile, e: IOException) -> Unit)?,
    private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<DocumentFile> {

    internal constructor(
        start: DocumentFile,
        direction: FileWalkDirection = FileWalkDirection.TOP_DOWN
    ) : this(start, direction, null, null, null)


    /**
     * Returns an iterator walking through files.
     */
    override fun iterator(): Iterator<DocumentFile> = DocumentTreeWalkIterator()


    //================================================================================
    // Walk state
    //================================================================================

    /**
     * Abstract class that encapsulates file visiting in some order, beginning from a given [root].
     */
    private abstract class WalkState(val root: DocumentFile) {
        /** Call of this function proceeds to a next file for visiting and returns it */
        abstract fun step(): DocumentFile?
    }


    /**
     * Subclass of [WalkState] for directory.
     */
    private abstract class DirectoryState(rootDir: DocumentFile) : WalkState(rootDir) {
        init {
//            if (_Assertions.ENABLED)
            assert(rootDir.isDirectory) { "rootDir must be verified to be directory beforehand." }
        }
    }


    /**
     * Subclass of [WalkState] for file.
     */
    private inner class SingleFileState(rootFile: DocumentFile) : WalkState(rootFile) {
        private var visited: Boolean = false

        init {
//                if (_Assertions.ENABLED)
            assert(rootFile.isFile) { "rootFile must be verified to be file beforehand." }
        }

        override fun step(): DocumentFile? {
            if (visited) return null
            visited = true
            return root
        }
    }


    //================================================================================
    // Iterator
    //================================================================================

    private inner class DocumentTreeWalkIterator : AbstractIterator<DocumentFile>() {

        // Stack of directory states, beginning from the start directory
        private val state = ArrayDeque<WalkState>()

        init {
            when {
                start.isDirectory -> state.push(directoryState(start))
                start.isFile -> state.push(SingleFileState(start))
                else -> done()
            }
        }

        private fun directoryState(root: DocumentFile): DirectoryState {
            return when (direction) {
                FileWalkDirection.TOP_DOWN -> TopDownDirectoryState(root)
                FileWalkDirection.BOTTOM_UP -> BottomUpDirectoryState(root)
            }
        }

        override fun computeNext() {
            val nextDocument = gotoNext()
            if (nextDocument != null) {
                setNext(nextDocument)
            } else {
                done()
            }
        }

        private tailrec fun gotoNext(): DocumentFile? {
            // Take next file from the top of the stack or return if there's nothing left
            val topState = state.peek() ?: return null
            val documentFile = topState.step()
            return if (documentFile == null) {
                // There is nothing more on the top of the stack, go back
                state.pop()
                gotoNext()
            } else {
                // Check that file/directory matches the filter
                if (documentFile == topState.root || !documentFile.isDirectory || state.size >= maxDepth) {
                    // Proceed to a root directory or a simple file
                    documentFile
                } else {
                    // Proceed to a sub-directory
                    state.push(directoryState(documentFile))
                    gotoNext()
                }
            }
        }

        /**
         * Visiting in top-down order.
         */
        private inner class TopDownDirectoryState(rootDir: DocumentFile) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: Array<DocumentFile>? = null

            private var fileIndex = 0

            /**
             * First root directory, then all children
             */
            override fun step(): DocumentFile? {
                if (!rootVisited) {
                    // First visit root
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    rootVisited = true
                    return root
                } else if (fileList == null || fileIndex < fileList!!.size) {
                    if (fileList == null) {
                        // Then read an array of files, if any
                        fileList = root.listFiles()
                        if (fileList == null) {
                            onFail?.invoke(root, IOException("Cannot list files in a tree URI: ${root.uri}"))
                        }
                        if (fileList == null || fileList!!.isEmpty()) {
                            onLeave?.invoke(root)
                            return null
                        }
                    }

                    // Then visit all files
                    return fileList!![fileIndex++]
                } else {
                    // That's all
                    onLeave?.invoke(root)
                    return null
                }
            }
        }

        /**
         * Visiting in bottom-up order.
         */
        private inner class BottomUpDirectoryState(rootDir: DocumentFile) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: Array<DocumentFile>? = null

            private var fileIndex = 0

            private var failed = false

            /**
             * First all children, then root directory.
             */
            override fun step(): DocumentFile? {
                if (!failed && fileList == null) {
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    fileList = root.listFiles()
                    if (fileList == null) {
                        onFail?.invoke(root, IOException("Cannot list files in a tree URI: ${root.uri}"))
                        failed = true
                    }
                }

                return if (fileList != null && fileIndex < fileList!!.size) {
                    // First visit all files
                    fileList!![fileIndex++]
                } else if (!rootVisited) {
                    // Then visit root
                    rootVisited = true
                    root
                } else {
                    // That's all
                    onLeave?.invoke(root)
                    null
                }
            }
        }

    }

    /**
     * Sets a [predicate], that is called on any entered directory before its files are visited
     * and before it is visited itself.
     *
     * If [predicate] returns `false` the directory is not entered and neither it nor its files are visited.
     */
    fun onEnter(predicate: (DocumentFile) -> Boolean): DocumentTreeWalk {
        return DocumentTreeWalk(start, direction, maxDepth = maxDepth,
            onEnter = predicate, onLeave = onLeave, onFail = onFail)
    }

    /**
     * Sets a [callback], that is called on any left directory after its files are visited
     * and after it is visited itself.
     */
    fun onLeave(callback: (DocumentFile) -> Unit): DocumentTreeWalk {
        return DocumentTreeWalk(start, direction, maxDepth = maxDepth,
            onEnter = onEnter, onLeave = callback, onFail = onFail)
    }

    /**
     * Set a [callback], that is called on a directory when it's impossible to get its file list.
     *
     * [onEnter] and [onLeave] callback functions are called even in this case.
     */
    fun onFail(callback: (DocumentFile, IOException) -> Unit): DocumentTreeWalk {
        return DocumentTreeWalk(start, direction, maxDepth = maxDepth,
            onEnter = onEnter, onLeave = onLeave, onFail = callback)
    }

    /**
     * Sets the maximum [depth] of a directory tree to traverse. By default there is no limit.
     *
     * The value must be positive and [Int.MAX_VALUE] is used to specify an unlimited depth.
     *
     * With a value of 1, walker visits only the origin directory and all its immediate children,
     * with a value of 2 also grandchildren, etc.
     */
    fun maxDepth(depth: Int): DocumentTreeWalk {
        if (depth <= 0)
            throw IllegalArgumentException("depth must be positive, but was $depth.")
        return DocumentTreeWalk(start, direction, onEnter, onLeave, onFail, depth)
    }

}


//================================================================================
// Extensions for DocumentFile
//================================================================================

/**
 * Gets a sequence for visiting this directory and all its content.
 *
 * @param direction walk direction, top-down (by default) or bottom-up.
 */
fun DocumentFile.walk(
    direction: FileWalkDirection = FileWalkDirection.TOP_DOWN
): DocumentTreeWalk = DocumentTreeWalk(this, direction)

/**
 * Gets a sequence for visiting this directory and all its content in top-down order.
 * Depth-first search is used and directories are visited before all their files.
 */
fun DocumentFile.walkTopDown(): DocumentTreeWalk = walk(FileWalkDirection.TOP_DOWN)

/**
 * Gets a sequence for visiting this directory and all its content in bottom-up order.
 * Depth-first search is used and directories are visited after all their files.
 */
fun DocumentFile.walkBottomUp(): DocumentTreeWalk = walk(FileWalkDirection.BOTTOM_UP)