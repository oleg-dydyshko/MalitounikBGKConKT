package by.carkva_gazeta.resources

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.carkva_gazeta.malitounik.MainActivity
import by.carkva_gazeta.malitounik.SettingsActivity
import kotlinx.android.synthetic.main.my_malitva_add.*
import java.io.File
import java.io.FileWriter
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

/**
 * Created by oleg on 14.6.16
 */
class MyNatatkiAdd : AppCompatActivity() {
    private var filename = ""
    private var redak = false
    private var dzenNoch = false
    private var md5sum = ""

    override fun onPause() {
        super.onPause()
        write()
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("savefile", true)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        md5sum = md5Sum("<MEMA></MEMA>")
        val k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        dzenNoch = k.getBoolean("dzen_noch", false)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        setContentView(R.layout.my_malitva_add)
        var title = resources.getString(by.carkva_gazeta.malitounik.R.string.MALITVA_ADD)
        val fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
        // Показываем клавиатуру
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        if (dzenNoch) { //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.colorprimary_material_dark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text)
                window.navigationBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text)
            }
        }
        EditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontBiblia)
        if (savedInstanceState != null) {
            filename = savedInstanceState.getString("filename") ?: ""
            redak = savedInstanceState.getBoolean("redak", false)
        } else {
            filename = intent.getStringExtra("filename") ?: ""
            redak = intent.getBooleanExtra("redak", false)
        }
        file.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontBiblia)
        if (dzenNoch) {
            EditText.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorIcons))
            file.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorIcons))
        } else {
            file.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text))
        }
        if (redak) {
            title = resources.getString(by.carkva_gazeta.malitounik.R.string.malitva_edit)
            val res = File("$filesDir/Malitva/$filename").readText().split("<MEMA></MEMA>").toTypedArray()
            if (res[1].contains("<RTE></RTE>")) {
                val start = res[1].indexOf("<RTE></RTE>")
                res[1] = res[1].substring(0, start)
                md5sum = md5Sum(res[0] + "<MEMA></MEMA>" + res[1].substring(0, start))
            } else {
                md5sum = md5Sum(res[0] + "<MEMA></MEMA>" + res[1])
            }
            EditText.setText(res[1])
            file.setText(res[0])
        }
        file.setSelection(file.text.toString().length)
        setTollbarTheme(title)
    }

    private fun setTollbarTheme(title: String) {
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
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title_toolbar.text = title
        if (dzenNoch) {
            toolbar.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
        }
    }

    private fun write() {
        var filename = file.text.toString()
        var imiafile = "Mae_malitvy"
        val string: String = EditText.text.toString()
        val gc = Calendar.getInstance() as GregorianCalendar
        val editMd5 = md5Sum("$filename<MEMA></MEMA>$string")
        if (md5sum != editMd5) {
            if (!redak) {
                var i = 1
                while (true) {
                    imiafile = "Mae_malitvy_$i"
                    val fileN = File(this.filesDir.toString() + "/Malitva/" + imiafile)
                    if (fileN.exists()) {
                        i++
                    } else {
                        break
                    }
                }
            }
            if (filename == "") {
                val mun = arrayOf("студзеня", "лютага", "сакавіка", "красавіка", "траўня", "чэрвеня", "ліпеня", "жніўня", "верасьня", "кастрычніка", "лістапада", "сьнежня")
                filename = gc[Calendar.DATE].toString() + " " + mun[gc[Calendar.MONTH]] + " " + gc[Calendar.YEAR] + " " + gc[Calendar.HOUR_OF_DAY] + ":" + gc[Calendar.MINUTE]
            }
            val file = if (redak) {
                File(this.filesDir.toString() + "/Malitva/" + this.filename)
            } else {
                File(this.filesDir.toString() + "/Malitva/" + imiafile)
            }
            val outputStream: FileWriter
            outputStream = FileWriter(file)
            outputStream.write(filename + "<MEMA></MEMA>" + string + "<RTE></RTE>" + gc.timeInMillis)
            outputStream.close()
            // Скрываем клавиатуру
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(EditText.windowToken, 0)
            redak = true
            this.filename = file.name
        }
    }

    private fun md5Sum(st: String): String {
        val digest: ByteArray
        val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")
        messageDigest.reset()
        messageDigest.update(st.toByteArray())
        digest = messageDigest.digest()
        val bigInt = BigInteger(1, digest)
        val md5Hex = StringBuilder(bigInt.toString(16))
        while (md5Hex.length < 32) {
            md5Hex.insert(0, "0")
        }
        return md5Hex.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("filename", filename)
        outState.putBoolean("redak", redak)
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
    }
}