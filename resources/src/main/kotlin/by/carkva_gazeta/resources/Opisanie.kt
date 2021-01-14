package by.carkva_gazeta.resources

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import by.carkva_gazeta.malitounik.MainActivity
import by.carkva_gazeta.malitounik.SettingsActivity
import by.carkva_gazeta.resources.databinding.AkafistUnderBinding
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class Opisanie : AppCompatActivity() {
    private var dzenNoch = false
    private var mun = Calendar.getInstance()[Calendar.MONTH]
    private var day = Calendar.getInstance()[Calendar.DATE]
    private var svity = ""
    private lateinit var binding: AkafistUnderBinding
    override fun onResume() {
        super.onResume()
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        val chin = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        dzenNoch = chin.getBoolean("dzen_noch", false)
        super.onCreate(savedInstanceState)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        if (chin.getBoolean("scrinOn", false)) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = AkafistUnderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val c = Calendar.getInstance()
        mun = intent.extras?.getInt("mun", c[Calendar.MONTH]) ?: c[Calendar.MONTH]
        day = intent.extras?.getInt("day", c[Calendar.DATE]) ?: c[Calendar.DATE]
        svity = intent.extras?.getString("svity", "") ?: ""
        var inputStream: InputStream? = null
        if (intent.extras?.getBoolean("glavnyia", false) == true) {
            if (svity.contains("уваход у ерусалім", true)) inputStream = resources.openRawResource(R.raw.opisanie_sv0)
            if (svity.contains("уваскрасеньне", true)) inputStream = resources.openRawResource(R.raw.opisanie_sv1)
            if (svity.contains("узьнясеньне", true)) inputStream = resources.openRawResource(R.raw.opisanie_sv2)
            if (svity.contains("зыход", true)) inputStream = resources.openRawResource(R.raw.opisanie_sv3)
            val resFile = day.toString() + "_" + mun
            if (resFile.contains("1_0")) inputStream = resources.openRawResource(R.raw.opisanie1_0)
            if (resFile.contains("2_1")) inputStream = resources.openRawResource(R.raw.opisanie2_1)
            if (resFile.contains("6_0")) inputStream = resources.openRawResource(R.raw.opisanie6_0)
            if (resFile.contains("6_7")) inputStream = resources.openRawResource(R.raw.opisanie6_7)
            if (resFile.contains("8_8")) inputStream = resources.openRawResource(R.raw.opisanie8_8)
            if (resFile.contains("14_8")) inputStream = resources.openRawResource(R.raw.opisanie14_8)
            if (resFile.contains("15_7")) inputStream = resources.openRawResource(R.raw.opisanie15_7)
            if (resFile.contains("25_2")) inputStream = resources.openRawResource(R.raw.opisanie25_2)
            if (resFile.contains("21_10")) inputStream = resources.openRawResource(R.raw.opisanie21_10)
            if (resFile.contains("25_11")) inputStream = resources.openRawResource(R.raw.opisanie25_11)
            if (inputStream != null) {
                val isr = InputStreamReader(inputStream)
                val reader = BufferedReader(isr)
                var line: String
                val builder = StringBuilder()
                reader.forEachLine {
                    line = it.replace("h3", "h6")
                    builder.append(line)
                }
                inputStream.close()
                binding.TextView.text = MainActivity.fromHtml(builder.toString())
            } else {
                binding.TextView.text = getString(by.carkva_gazeta.malitounik.R.string.opisanie_error)
            }
        } else {
            inputStream = when (mun) {
                0 -> resources.openRawResource(R.raw.opisanie1)
                1 -> resources.openRawResource(R.raw.opisanie2)
                2 -> resources.openRawResource(R.raw.opisanie3)
                3 -> resources.openRawResource(R.raw.opisanie4)
                4 -> resources.openRawResource(R.raw.opisanie5)
                5 -> resources.openRawResource(R.raw.opisanie6)
                6 -> resources.openRawResource(R.raw.opisanie7)
                7 -> resources.openRawResource(R.raw.opisanie8)
                8 -> resources.openRawResource(R.raw.opisanie9)
                9 -> resources.openRawResource(R.raw.opisanie10)
                10 -> resources.openRawResource(R.raw.opisanie11)
                11 -> resources.openRawResource(R.raw.opisanie12)
                else -> resources.openRawResource(R.raw.opisanie1)
            }
            val isr = InputStreamReader(inputStream)
            val reader = BufferedReader(isr)
            val builder = StringBuilder()
            reader.forEachLine {
                builder.append(it)
            }
            inputStream.close()
            val dataR: String = if (day < 10) "0$day" else day.toString()
            mun++
            val munR: String = if (mun < 10) "0$mun" else mun.toString()
            var res = builder.toString()
            val tN = res.indexOf("<div id=\"$dataR$munR\">")
            val tK = res.indexOf("</div>", tN)
            res = res.substring(tN, tK + 6)
            res = res.replace("<div id=\"$dataR$munR\">", "")
            res = res.replace("<h3 class=\"blocks\">", "<p><strong>")
            res = res.replace("</h3>", "</strong>")
            res = res.replace("</div>", "")
            binding.TextView.text = MainActivity.fromHtml(res)
        }
        setTollbarTheme()
    }

    private fun setTollbarTheme() {
        binding.titleToolbar.setOnClickListener {
            binding.titleToolbar.setHorizontallyScrolling(true)
            binding.titleToolbar.freezesText = true
            binding.titleToolbar.marqueeRepeatLimit = -1
            if (binding.titleToolbar.isSelected) {
                binding.titleToolbar.ellipsize = TextUtils.TruncateAt.END
                binding.titleToolbar.isSelected = false
            } else {
                binding.titleToolbar.ellipsize = TextUtils.TruncateAt.MARQUEE
                binding.titleToolbar.isSelected = true
            }
        }
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = resources.getText(by.carkva_gazeta.malitounik.R.string.zmiest)
        if (dzenNoch) {
            binding.toolbar.popupTheme = by.carkva_gazeta.malitounik.R.style.AppCompatDark
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val infl = menuInflater
        infl.inflate(by.carkva_gazeta.malitounik.R.menu.opisanie, menu)
        for (i in 0 until menu.size()) {
            val item: MenuItem = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == by.carkva_gazeta.malitounik.R.id.action_share) {
            val sendIntent = Intent(Intent.ACTION_SEND)
            var sviatylink = ""
            if (svity != "")
                sviatylink = "&sviata=true"
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://carkva-gazeta.by/share/index.php?pub=3$sviatylink&date=$day&month=$mun")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, null))
        }
        return super.onOptionsItemSelected(item)
    }
}