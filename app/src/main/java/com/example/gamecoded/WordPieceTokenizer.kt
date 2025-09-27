package com.example.gamecoded

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

class WordPieceTokenizer(private val vocab: Map<String, Int>, private val unkToken: String = "[UNK]") {
    private val maxInputCharsPerWord = 100
    private val isPunctuation = Pattern.compile("\\p{Punct}")

    fun tokenizeBasic(text: String, doLowerCase: Boolean = true): List<String> {
        var s = text
        if (doLowerCase) s = s.lowercase()
        // simple whitespace split
        val words = mutableListOf<String>()
        val split = s.trim().split(Regex("\\s+"))
        for (word in split) {
            if (word.isEmpty()) continue
            // split punctuation out (basic)
            val m = isPunctuation.matcher(word)
            if (m.find()) {
                // naive: keep punctuation attached — WordPiece will try subwords
                words.add(word)
            } else {
                words.add(word)
            }
        }
        return words
    }

    fun wordPieceTokenize(word: String): List<String> {
        val tokens = mutableListOf<String>()
        if (word.length > maxInputCharsPerWord) {
            tokens.add(unkToken)
            return tokens
        }
        var isBad = false
        var start = 0
        val subTokens = mutableListOf<String>()
        while (start < word.length) {
            var end = word.length
            var curSubstr: String? = null
            while (start < end) {
                var substr = word.substring(start, end)
                if (start > 0) substr = "##$substr"
                if (vocab.containsKey(substr)) {
                    curSubstr = substr
                    break
                }
                end -= 1
            }
            if (curSubstr == null) {
                isBad = true
                break
            }
            subTokens.add(curSubstr)
            start = end
        }
        if (isBad) {
            tokens.add(unkToken)
        } else {
            tokens.addAll(subTokens)
        }
        return tokens
    }

    fun encode(text: String, seqLen: Int = 128, clsToken: String = "[CLS]", sepToken: String = "[SEP]"): LongArray {
        val basic = tokenizeBasic(text)
        val pieces = mutableListOf<String>()
        for (w in basic) {
            pieces.addAll(wordPieceTokenize(w))
        }
        // build token ids
        val ids = mutableListOf<Long>()
        vocab[clsToken]?.let { ids.add(it.toLong()) }
        for (p in pieces) {
            val id = vocab[p] ?: vocab[unkToken]
            ids.add((id ?: vocab[unkToken]!!).toLong())
            if (ids.size >= seqLen - 1) break
        }
        vocab[sepToken]?.let { ids.add(it.toLong()) } ?: ids.add(vocab[unkToken]!!.toLong())

        // pad to seqLen
        while (ids.size < seqLen) ids.add(0L) // pad id = 0
        return ids.toLongArray()
    }

    companion object {
        fun fromAssets(context: Context, vocabAssetPath: String = "tokenizer/vocab.txt"): WordPieceTokenizer {
            val map = mutableMapOf<String, Int>()
            val stream = context.assets.open(vocabAssetPath)
            BufferedReader(InputStreamReader(stream)).use { br ->
                var line: String?
                var idx = 0
                while (br.readLine().also { line = it } != null) {
                    map[line!!.trim()] = idx++
                }
            }
            return WordPieceTokenizer(map)
        }
    }
}