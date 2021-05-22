package by.carkva_gazeta.resources

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.malitounik.MainActivity
import by.carkva_gazeta.malitounik.SettingsActivity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class DialogLiturgia : DialogFragment() {
    private var chast = 1
    private lateinit var ab: AlertDialog.Builder

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainActivity.dialogVisable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chast = arguments?.getInt("chast") ?: 1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { it ->
            MainActivity.dialogVisable = true
            val k = it.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            val dzenNoch = k.getBoolean("dzen_noch", false)
            var style = by.carkva_gazeta.malitounik.R.style.AlertDialogTheme
            if (dzenNoch) style = by.carkva_gazeta.malitounik.R.style.AlertDialogThemeBlack
            ab = AlertDialog.Builder(it, style)
            val builder = StringBuilder()
            val linearLayout = LinearLayout(it)
            linearLayout.orientation = LinearLayout.VERTICAL
            val textViewZaglavie = TextView(it)
            if (dzenNoch) textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorPrimary_black)) else textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorPrimary))
            val density = resources.displayMetrics.density
            val realpadding = (10 * density).toInt()
            textViewZaglavie.setPadding(realpadding, realpadding, realpadding, realpadding)
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            textViewZaglavie.typeface = MainActivity.createFont(it,  Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorWhite))
            linearLayout.addView(textViewZaglavie)
            val r = it.resources
            var inputStream: InputStream = r.openRawResource(R.raw.bogashlugbovya1_1)
            when (chast) {
                1 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_1)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.ps_102)
                }
                2 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_2)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.ps_91)
                }
                3 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_3)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.ps_145)
                }
                4 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_4)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.ps_92)
                }
                5 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_5)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.mc_5_3_12)
                }
                6 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_6)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.malitva_za_pamerlyx)
                }
                7 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_7)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.malitva_za_paclicanyx)
                }
                8 -> {
                    textViewZaglavie.text = by.carkva_gazeta.malitounik.R.string.czytanne.toString().uppercase()
                    val zch = ZmenyiaChastki(it)
                    builder.append(zch.sviatyiaView(1))
                }
                9 -> {
                    textViewZaglavie.text = by.carkva_gazeta.malitounik.R.string.czytanne.toString().uppercase()
                    val zch = ZmenyiaChastki(it)
                    builder.append(zch.sviatyiaView(0))
                }
                10 -> {
                    inputStream = r.openRawResource(R.raw.bogashlugbovya1_8)
                    textViewZaglavie.setText(by.carkva_gazeta.malitounik.R.string.ps_94)
                }
            }
            if (!(chast == 8 || chast == 9)) {
                val isr = InputStreamReader(inputStream)
                val reader = BufferedReader(isr)
                var line: String
                reader.forEachLine {
                    line = it
                    if (dzenNoch) line = line.replace("#d00505", "#f44336")
                    builder.append(line).append("\n")
                }
                inputStream.close()
            }
            val scrollView = ScrollView(it)
            linearLayout.addView(scrollView)
            scrollView.isVerticalScrollBarEnabled = false
            scrollView.setPadding(realpadding, realpadding, realpadding, realpadding)
            val textView = TextView(it)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE))
            if (dzenNoch) textView.setTextColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorWhite)) else textView.setTextColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorPrimary_text))
            textView.text = MainActivity.fromHtml(builder.toString())
            scrollView.addView(textView)
            ab.setView(linearLayout)
            ab.setPositiveButton(it.resources.getString(by.carkva_gazeta.malitounik.R.string.ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        }
        return ab.create()
    }

    companion object {
        fun getInstance(chast: Int): DialogLiturgia {
            val instance = DialogLiturgia()
            val args = Bundle()
            args.putInt("chast", chast)
            instance.arguments = args
            return instance
        }
    }
}