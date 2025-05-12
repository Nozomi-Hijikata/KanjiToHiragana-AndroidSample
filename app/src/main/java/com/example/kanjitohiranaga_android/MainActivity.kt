package com.example.kanjitohiranaga_android

import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.TtsSpan
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kanjitohiranaga_android.ui.theme.KanjiToHiranagaAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanjiToHiranagaAndroidTheme {
                PhoneticDemoScreen()
            }
        }
    }
}

/* ───────────── UI Composable ───────────── */

@Composable
private fun PhoneticDemoScreen() {
    var phoneticLast by remember { mutableStateOf("") }
    var phoneticAll by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(Modifier.padding(16.dp)) {

        /* ① 日本語氏名入力用 EditText（IME からふりがな要求） */
        AndroidView(
            factory = { ctx ->
                EditText(ctx).apply {
                    // 高さを明示的に設定（MATCH_PARENTではなく固定値）
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 150)
                    hint = "姓・名（漢字）"
                    /* IME にふりがな返却を要求 */
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                    privateImeOptions =
                        "com.google.android.inputmethod.latin.requestPhoneticOutput"
                    
                    // フォーカスを要求
                    requestFocus()
                    
                    addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val hira = s?.extractPhonetic()
                            if (hira != null) {
                                phoneticLast = hira
                                phoneticAll += hira
                            }
                        }
                        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                    })
                }
            },
            update = { /* no‑op */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        /* ② 直近のふりがな */
        Text(
            text = if (phoneticLast.isEmpty()) "フリガナ: (未取得)" else "フリガナ: $phoneticLast",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

/* ───────────── 拡張関数 ───────────── */

/** EditText に入ってきた Spanned から TtsSpan の phonetic を抽出して除去 */
private fun CharSequence.extractPhonetic(): String? {
    if (this !is SpannableStringBuilder) return null
    val spans = getSpans(0, length, TtsSpan::class.java)
    if (spans.size == 1 && spans[0].type == TtsSpan.TYPE_TEXT) {
        val span = spans[0]
        val kana = span.args.getString(TtsSpan.ARG_TEXT)
        Log.v("PHON", "phonetic=$kana")
        removeSpan(span)   // 重複取得を防ぐため削除
        return kana
    }
    return null
}
