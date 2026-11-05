package com.odkazlabs.thumbsupkeyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class ThumbsUpImeService : InputMethodService() {

    private var currentThumb = "👍"
    private val rowCounts = listOf(10, 9, 7)

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1F1B24"))
            setPadding(12, 16, 12, 28)
        }

        // 3 Reihen voller Daumen-Hoch-Tasten (wie QWERTZ)
        for (count in rowCounts) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 10 }
            }

            for (i in 0 until count) {
                val btn = createKeyButton(currentThumb, bgColor = "#332940", textColor = "#FFFFFF", weight = 1f) {
                    currentInputConnection?.commitText(currentThumb, 1)
                }
                row.addView(btn)
            }
            root.addView(row)
        }

        // Unterste Reihe: Nur Leertaste, Löschen, Enter (plus 1 Daumen links wie gewünscht)
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val thumbBtn = createKeyButton(currentThumb, bgColor = "#332940", textColor = "#FFFFFF", weight = 1.2f) {
            currentInputConnection?.commitText(currentThumb, 1)
        }

        val spaceBtn = createKeyButton("Leertaste", bgColor = "#4A3B5C", textColor = "#F8BBD0", weight = 3.5f) {
            currentInputConnection?.commitText(" ", 1)
        }

        val deleteBtn = createKeyButton("⌫", bgColor = "#5C2B3E", textColor = "#FF80AB", weight = 1.3f) {
            val ic = currentInputConnection ?: return@createKeyButton
            val selected = ic.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                ic.commitText("", 1)
            } else {
                // Löscht auch 2-Byte Surrogate Pairs (Emojis) sauber
                val before = ic.getTextBeforeCursor(2, 0) ?: ""
                if (before.length >= 2 && Character.isSurrogatePair(before[0], before[1])) {
                    ic.deleteSurroundingText(2, 0)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
        }

        val enterBtn = createKeyButton("↵", bgColor = "#EC407A", textColor = "#FFFFFF", weight = 1.5f) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }

        bottomRow.addView(thumbBtn)
        bottomRow.addView(spaceBtn)
        bottomRow.addView(deleteBtn)
        bottomRow.addView(enterBtn)
        root.addView(bottomRow)

        return root
    }

    private fun createKeyButton(
        label: String,
        bgColor: String,
        textColor: String,
        weight: Float,
        onClick: () -> Unit
    ): Button {
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor(bgColor))
            cornerRadius = 18f
        }
        return Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(Color.parseColor(textColor))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT_BOLD
            background = bg
            setPadding(4, 24, 4, 24)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight).apply {
                setMargins(5, 4, 5, 4)
            }
            setOnClickListener { onClick() }
        }
    }
}
