package by.carkva_gazeta.malitounik

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.akafist_list.*

/**
 * Created by oleg on 30.5.16
 */
class TonNaKoznyDzen : AppCompatActivity() {
    private var mLastClickTime: Long = 0
    private val data = arrayOf("ПАНЯДЗЕЛАК\nСлужба сьвятым анёлам", "АЎТОРАК\nСлужба сьвятому Яну Хрысьціцелю", "СЕРАДА\nСлужба Найсьвяцейшай Багародзіцы і Крыжу", "ЧАЦЬВЕР\nСлужба апосталам і сьвятому Мікалаю", "ПЯТНІЦА\nСлужба Крыжу Гасподняму", "СУБОТА\nСлужба ўсім сьвятым і памёрлым")

    override fun onCreate(savedInstanceState: Bundle?) {
        val chin = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        val dzenNoch = chin.getBoolean("dzen_noch", false)
        if (dzenNoch) setTheme(R.style.AppCompatDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.akafist_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title_toolbar.setOnClickListener {
            title_toolbar.setHorizontallyScrolling(true)
            title_toolbar.freezesText = true
            title_toolbar.marqueeRepeatLimit = -1
            if (title_toolbar.isSelected) {
                title_toolbar.ellipsize = TextUtils.TruncateAt.END
                title_toolbar.isSelected = false
            } else {
                title_toolbar.ellipsize = TextUtils.TruncateAt.MARQUEE
                title_toolbar.isSelected = true
            }
        }
        title_toolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        title_toolbar.text = resources.getText(R.string.ton_sh)
        if (dzenNoch) {
            toolbar.popupTheme = R.style.AppCompatDark
            toolbar.setBackgroundResource(R.color.colorprimary_material_dark)
            title_toolbar.setBackgroundResource(R.color.colorprimary_material_dark)
        }
        val adapter = MenuListAdaprer(this, data)
        ListView.adapter = adapter
        ListView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@OnItemClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            if (MainActivity.checkmoduleResources(this)) {
                val intent = Intent(this, Class.forName("by.carkva_gazeta.resources.Ton"))
                intent.putExtra("ton", position + 1)
                intent.putExtra("ton_naidzelny", false)
                startActivity(intent)
            } else {
                val dadatak = DialogInstallDadatak()
                dadatak.show(supportFragmentManager, "dadatak")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        overridePendingTransition(R.anim.alphain, R.anim.alphaout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}