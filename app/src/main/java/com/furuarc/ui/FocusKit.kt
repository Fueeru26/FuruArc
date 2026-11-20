package com.furuarc.ui

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.ref.WeakReference

/**
 * Text-field focus handling for the whole app: while a field is in "typing mode" (colored border + cursor):
 * back, tapping anywhere outside the field, or the keyboard being hidden returns it to the normal (gray) look
 * and hides the keyboard.
 */
object FocusHub {
    private var nativeView: WeakReference<View>? = null
    private var composeFocused by mutableStateOf(false)
    private var nativeFocused by mutableStateOf(false)

    /** True while any text field has focus. */
    val focused: Boolean get() = composeFocused || nativeFocused

    fun onNativeFocus(v: View, f: Boolean) {
        if (f) { nativeView = WeakReference(v); nativeFocused = true }
        else if (nativeView?.get() === v) { nativeView = null; nativeFocused = false }
    }

    fun onComposeFocus(f: Boolean) { composeFocused = f }

    /** Remove focus from the active field and hide the keyboard. [host] = the Compose host view. */
    fun clear(host: View, fm: FocusManager, hideKeyboard: Boolean = true) {
        val nv = nativeView?.get()
        nv?.clearFocus()
        fm.clearFocus(true)
        // clearing an EditText's focus lets Android hand the focus to another field: give it to the Compose host view
        host.requestFocus()
        if (hideKeyboard) {
            val imm = host.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow((nv ?: host).windowToken, 0)
        }
        nativeFocused = false
        composeFocused = false
        nativeView = null
    }

    /** True if the window position ([x],[y]) (px) is inside the focused native field. */
    internal fun hits(x: Int, y: Int): Boolean {
        val v = nativeView?.get() ?: return false
        val loc = IntArray(2)
        v.getLocationInWindow(loc)
        return x >= loc[0] && x <= loc[0] + v.width && y >= loc[1] && y <= loc[1] + v.height
    }
}

/** Compose text fields report their focus to [FocusHub]. */
fun Modifier.reportFocus(): Modifier = onFocusChanged { FocusHub.onComposeFocus(it.isFocused) }

/**
 * Root modifier: a press anywhere outside the focused field ends typing mode (the press itself is not consumed, so buttons still work).
 * Also ends it when the keyboard is hidden (e.g. by the system back gesture).
 */
@Composable
fun Modifier.clearFocusOnOutsideTap(): Modifier {
    val host = LocalView.current
    val fm = LocalFocusManager.current
    DisposableEffect(host) {
        var wasVisible = false
        val l = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val vis = ViewCompat.getRootWindowInsets(host)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (wasVisible && !vis && FocusHub.focused) FocusHub.clear(host, fm, hideKeyboard = false)
            wasVisible = vis
        }
        host.viewTreeObserver.addOnGlobalLayoutListener(l)
        onDispose { if (host.viewTreeObserver.isAlive) host.viewTreeObserver.removeOnGlobalLayoutListener(l) }
    }
    return this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val e = awaitPointerEvent(PointerEventPass.Initial)
                if (e.type == PointerEventType.Press && FocusHub.focused) {
                    val pos: Offset = e.changes.firstOrNull()?.position ?: continue
                    val wl = IntArray(2)
                    host.getLocationInWindow(wl)
                    if (!FocusHub.hits((pos.x + wl[0]).toInt(), (pos.y + wl[1]).toInt())) FocusHub.clear(host, fm)
                }
            }
        }
    }
}
