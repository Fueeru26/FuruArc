package com.furuarc.ui

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.furuarc.R
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.delay

/**
 * Text field using the native Android EditText (not Compose BasicTextField), so keyboard handling
 * (e.g. holding the delete key in Gboard) is exactly like other apps. The look imitates an outlined field:
 * theme-colored border, label shrinks upward on focus/filled.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NativeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    mono: Boolean,
    number: Boolean,
    multiline: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    val p = LocalPalette.current
    val density = LocalDensity.current.density
    var focused by remember { mutableStateOf(false) }
    val floated = focused || value.isNotEmpty()
    val latest by rememberUpdatedState(onValueChange)
    val requester = remember { BringIntoViewRequester() }
    // EditText is not part of Compose: ask the list to scroll to this field after the keyboard appears
    LaunchedEffect(focused) {
        if (focused) {
            delay(350)
            requester.bringIntoView()
        }
    }
    val accentArgb = p.accent.toArgb()
    Row(
        Modifier
            .fillMaxWidth()
            // the Compose field leaves 8dp above the border for the label that overlaps the border; match it so the position is the same
            .padding(top = 8.dp)
            .heightIn(min = 56.dp)
            .background(p.field, RoundedCornerShape(ControlRadius))
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) p.accent else p.fieldBorder,
                shape = RoundedCornerShape(ControlRadius),
            )
            .bringIntoViewRequester(requester),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            Text(
                label,
                color = if (focused) p.accent else p.muted,
                fontSize = if (floated) 12.sp else 16.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = if (floated) 6.dp else 16.dp),
            )
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { c ->
                    val et = EditText(c)
                    val syncing = BooleanArray(1)
                    et.tag = syncing
                    et.background = null
                    et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    et.setPadding((16 * density).toInt(), (24 * density).toInt(), (16 * density).toInt(), (10 * density).toInt())
                    et.typeface = if (mono) Typeface.MONOSPACE else (ResourcesCompat.getFont(c, R.font.qanelas_regular) ?: Typeface.DEFAULT)
                    et.isVerticalScrollBarEnabled = false
                    if (number) {
                        et.inputType = InputType.TYPE_CLASS_NUMBER
                        et.setSingleLine(true)
                    } else if (multiline) {
                        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        et.setSingleLine(false)
                        et.maxLines = 8
                        et.gravity = Gravity.TOP or Gravity.START
                        // a finger swipe inside the field scrolls its own text (not the page) as long as it can still scroll
                        et.setOnTouchListener { v, ev ->
                            when (ev.actionMasked) {
                                android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_MOVE ->
                                    if (v.canScrollVertically(1) || v.canScrollVertically(-1)) v.parent?.requestDisallowInterceptTouchEvent(true)
                                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL ->
                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                            false
                        }
                    } else {
                        et.inputType = InputType.TYPE_CLASS_TEXT
                        et.setSingleLine(true)
                    }
                    et.highlightColor = (accentArgb and 0x00FFFFFF) or 0x59000000
                    if (Build.VERSION.SDK_INT >= 29) {
                        val cursor = GradientDrawable()
                        cursor.setColor(accentArgb)
                        cursor.setSize((2 * density).toInt().coerceAtLeast(2), (22 * density).toInt())
                        et.textCursorDrawable = cursor
                    }
                    et.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            if (!syncing[0]) latest(s?.toString() ?: "")
                        }
                    })
                    et.setOnFocusChangeListener { v, f -> focused = f; FocusHub.onNativeFocus(v, f) }
                    et
                },
                update = { et ->
                    et.setTextColor(p.text.toArgb())
                    val cur = et.text?.toString() ?: ""
                    if (cur != value) {
                        val flag = et.tag as BooleanArray
                        flag[0] = true
                        et.setText(value)
                        et.setSelection(value.length)
                        flag[0] = false
                    }
                },
            )
        }
        if (trailing != null) trailing()
    }
}
